package com.angelp.purchasehistory.web.clients;


import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.ScheduledExpenseDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.TriggerPurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduledExpenseClient extends HttpClient {
    private static final String TAG = ScheduledExpenseClient.class.getSimpleName();

    @Inject
    public ScheduledExpenseClient() {
    }

    public ScheduledExpenseView createScheduledExpense(ScheduledExpenseDTO body) {
        try (Response res = post(BACKEND_URL + "/scheduled-expense", body)) {
            ResponseBody responseBody = res.body();
            if (res.isSuccessful() && responseBody != null) {
                String json = responseBody.string();
                Log.d("httpResponse", "Created Scheduled Expense: " + json);
                return gson.fromJson(json, ScheduledExpenseView.class);
            } else {
                throw new IOException("Failed to create scheduled expense");
            }
        } catch (IOException e) {
            Log.e(TAG, "createScheduledExpense ERROR: " + e.getMessage());
        }
        return null;
    }

    public PurchaseView triggerScheduledPurchase(TriggerPurchaseDTO body) {
        try (Response res = post(BACKEND_URL + "/scheduled-expense/trigger", body)) {
            ResponseBody responseBody = res.body();
            if (res.isSuccessful() && responseBody != null) {
                String json = responseBody.string();
                Log.d("httpResponse", "Triggered Scheduled Purchase: " + json);
                return gson.fromJson(json, PurchaseView.class);
            } else {
                throw new IOException("Failed to trigger scheduled purchase");
            }
        } catch (IOException e) {
            Log.e(TAG, "triggerScheduledPurchase ERROR: " + e.getMessage());
        }
        return null;
    }

    public List<ScheduledExpenseView> findAllForUser() {
        try (Response res = get(BACKEND_URL + "/scheduled-expense/all")) {
            ResponseBody responseBody = res.body();
            if (res.isSuccessful() && responseBody != null) {
                String json = responseBody.string();
                Log.d("httpResponse", "Find all scheduled expenses: " + json);
                return Arrays.stream(gson.fromJson(json, ScheduledExpenseView[].class)).collect(Collectors.toList());
            } else {
                throw new IOException("Failed to find all scheduled expenses");
            }
        } catch (IOException e) {
            Log.e(TAG, "findAllForUser ERROR: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteScheduledExpense(Long id) {
        try (Response res = delete(BACKEND_URL + "/scheduled-expense/delete?id=" + id)) {
            if (res.isSuccessful()) {
                Log.i("httpResponse", "Deleted scheduled expense: Success");
                return true;
            } else {
                throw new IOException("Failed to delete scheduled expense");
            }
        } catch (IOException e) {
            Log.e(TAG, "deleteScheduledExpense ERROR: " + e.getMessage());
        }
        return false;
    }

    public ScheduledExpenseView editScheduledExpense(ScheduledExpenseDTO dto, Long id) {
        try (Response res = put(BACKEND_URL + "/scheduled-expense/" + id, dto)) {
            ResponseBody responseBody = res.body();
            if (res.isSuccessful() && responseBody != null) {
                String json = responseBody.string();
                Log.d("httpResponse", "Edited Scheduled Expense: " + json);
                return gson.fromJson(json, ScheduledExpenseView.class);
            } else {
                throw new IOException("Failed to edit scheduled expense");
            }
        } catch (IOException e) {
            Log.e(TAG, "editScheduledExpense ERROR: " + e.getMessage());
        }
        return null;
    }
}
