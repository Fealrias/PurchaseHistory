package com.angelp.purchasehistory.web.clients;

import android.content.Context;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.util.Log;

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.Category;
import com.angelp.purchasehistory.data.model.PurchaseResponse;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PageView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.PurchaseListView;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Singleton
public class PurchaseClient extends HttpClient {
    private final String TAG = this.getClass().getSimpleName();
    private Cache cache;

    @Inject
    public PurchaseClient() {
    }

    @Override
    protected @NotNull OkHttpClient getHttpClient() {
        Context context = PurchaseHistoryApplication.getContext();
        long cacheQuotaBytes = getCacheQuotaBytes(context);
        File cacheFile = new File(context.getCacheDir(), "/purchases");
        this.cache = new Cache(cacheFile, cacheQuotaBytes);
        return new OkHttpClient().newBuilder()
                .hostnameVerifier((hostname, session) -> true)
                .addInterceptor(authInterceptor)
                .cache(cache)
                .build();
    }

    private long getCacheQuotaBytes(Context context) {
        long cacheQuotaBytes = 1024*1024*5;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            UUID id = storageManager.getUuidForPath(context.getCacheDir());
            cacheQuotaBytes = storageManager.getCacheQuotaBytes(id);
            if (cacheQuotaBytes > 1024*1024*2) cacheQuotaBytes-=512; // to stay under the quota
        } catch (IOException e) {
            Log.e(TAG, "PurchaseClient: failed to get cache quota. Using Default 5 MB");
        }
        return cacheQuotaBytes;
    }

    public PurchaseView createPurchase(PurchaseDTO purchaseDTO) {
        try (Response res = post(BACKEND_URL + "/purchase", purchaseDTO)) {
            return utils.getBody(res, PurchaseView.class);
        } catch (IOException ignored) {
        } finally {
            cleanCache(BACKEND_URL + "/purchase");
        }
        return null;
    }

    public void validatePurchase(PurchaseDTO purchaseDTO) {
        try (Response res = post(BACKEND_URL + "/purchase/check", purchaseDTO)) {
            utils.getBody(res, PurchaseView.class);
        } catch (IOException e) {
            Log.e(TAG, "validatePurchase: ", e);
        }
    }

    public PurchaseView editPurchase(PurchaseDTO purchaseDTO, Long id) {
        try (Response res = put(BACKEND_URL + "/purchase/" + id, purchaseDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Edit Purchase: " + json);
                return gson.fromJson(json, PurchaseResponse.class).toPurchaseView();
            } else throw new IOException("Failed to edit purchase");
        } catch (IOException ignored) {
        } finally {
            cleanCache(BACKEND_URL + "/purchase");
        }
        return null;
    }

    public List<PurchaseView> getAllPurchases() throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, PurchaseResponse[].class)).map(PurchaseResponse::toPurchaseView).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public PurchaseListView getAllPurchases(PurchaseFilter filter) throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase/filtered?" + filter)) {
            return utils.getBody(res, PurchaseListView.class);
        } catch (IOException | JsonParseException | NullPointerException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public PageView<PurchaseView> getPurchases(PurchaseFilter filter) throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase/paged?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, new TypeToken<PageView<PurchaseView>>() {
                    }.getType());
                } else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public CalendarReport getCategorizedCalendarReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/purchase/analytics/calendar?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, CalendarReport.class);
                } else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getCalendarReport ERROR: " + e.getMessage());
        }
        return null;
    }

    public CalendarReport getCalendarReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/purchase/analytics/monthly?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, CalendarReport.class);
                } else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getCalendarReport ERROR: " + e.getMessage());
        }
        return null;
    }


    public List<CategoryView> getAllCategories() {
        try (Response res = get(BACKEND_URL + "/category")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Get all categories: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, CategoryView[].class)).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllCategories ERROR: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public Category createCategory(CategoryDTO categoryDTO) {
        try (Response res = post(BACKEND_URL + "/category", categoryDTO)) {
            return utils.getBody(res, Category.class);
        } catch (IOException ignored) {
        } finally {
            cleanCache(BACKEND_URL + "/category");
        }
        return null;
    }

    public Category editCategory(int id, CategoryDTO categoryDTO) {
        try (Response res = put(BACKEND_URL + "/category/" + id, categoryDTO)) {
            return utils.getBody(res, Category.class);
        } catch (IOException ignored) {
        } finally {
            cleanCache();
        }
        return null;
    }

    public void deleteCategory(Long id) {
        try (Response res = delete(BACKEND_URL + "/category/" + id)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "Delete Category: " + json);
            } else throw new IOException("Failed to delete category");
        } catch (IOException ignored) {
        } finally {
            cleanCache();
        }
    }

    public void getExportedCsv(Context context, Uri saveLocation) {
        try (Response res = get(BACKEND_URL + "/purchase/export")) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                createFile(context, body.bytes(), saveLocation);
            } else {
                if (body != null) {
                    ErrorResponse errorResponse = gson.fromJson(body.string(), ErrorResponse.class);
                    body.close();
                    if ("NO_PURCHASES_TO_SHOW".equals(errorResponse.getDetail()))
                        throw new WebException(R.string.no_purchases_to_show);
                }
                throw new WebException(R.string.could_not_download_file);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error downloading CSV: " + e.getMessage());
        }
    }

    private void createFile(Context context, byte[] bytes, Uri location) {
        if (location != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(location)) {
                if (outputStream != null) {
                    outputStream.write(bytes);
                    Log.i(TAG, "CSV file saved successfully: " + location.getPath());
                } else {
                    Log.e(TAG, "OutputStream is null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing CSV file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to create file URI");
        }
    }

    public CategoryAnalyticsReport getCategoryAnalyticsReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/category/analytics?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                body.close();
                Log.d("httpResponse", "getCategoryAnalyticsReport : " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, CategoryAnalyticsReport.class);
                }
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new IOException(String.valueOf(errorResponse));
                }
            }
        } catch (IOException | JsonParseException | NullPointerException e) {
            Log.e(TAG, "analytics ERROR: " + e.getMessage());
        }
        return null;
    }

    public boolean deletePurchase(Long purchaseId) {
        try (Response res = delete(BACKEND_URL + "/purchase?id=" + purchaseId)) {
            if (res.isSuccessful()) {
                Log.i("httpResponse", "delete purchase : Success");
                return true;
            }
            if (res.body() != null && res.body().contentLength() > 0) {
                ErrorResponse errorResponse = gson.fromJson(res.body().string(), ErrorResponse.class);
                res.body().close();
                throw new IOException(errorResponse.getDetail());
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        } finally {
            cleanCache(BACKEND_URL + "/purchase");
        }
        return false;
    }
    public void cleanCache(){
        try {
            cache.evictAll();
            Log.i(TAG, "cleanCache: SUCCESS");
        } catch (IOException e) {
            Log.e(TAG, "cleanCache: ", e);
        }
    }
    public void cleanCache(String url){
        try {
            Iterator<String> urlIterator = cache.urls();
            while (urlIterator.hasNext()) {
                if (urlIterator.next().startsWith(url)) {
                    urlIterator.remove();
                }
            }
            Log.i(TAG, "cleanCache:"+url+" SUCCESS");
        } catch (IOException e) {
            Log.e(TAG, "cleanCache: ", e);
        }
    }
}
