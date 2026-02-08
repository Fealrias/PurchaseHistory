package com.fealrias.purchasehistory.web.clients;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fealrias.purchasehistory.R;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VersionUpdater extends HttpClient {
    private static final String TAG = "version-update";
    public static final String REPO_DOWNLOAD_URL = "https://github.com/Fealrias/PurchaseHistory/releases/download/v";

    @Inject
    public VersionUpdater() {
    }

    public void checkForUpdate(Context context, Runnable onCancel) {

        new AppUpdaterUtils(context)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("Fealrias", "PurchaseHistory")
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(Update update, Boolean isUpdateAvailable) {
                        Log.d(TAG, "URL " + update.getUrlToDownload());
                        Log.d(TAG, "Is update available? " + isUpdateAvailable);
                        Log.d(TAG, "Release notes: " + update.getReleaseNotes());
                        Log.d(TAG, "Version Code:  " + update.getLatestVersion());
                        if (isUpdateAvailable) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BaseDialogStyle);
                            builder.setTitle(R.string.update_available);
                            builder.setMessage(context.getString(R.string.update_available_description, update.getLatestVersion()));
                            builder.setPositiveButton(R.string.download, (dialog, which) -> {
                                download(update, context);
                            });
                            builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());
                            builder.setOnDismissListener((d) -> onCancel.run());
                            builder.show();
                        } else {
                            onCancel.run();
                        }

                    }

                    /**
                     * @param error
                     */
                    @Override
                    public void onFailed(AppUpdaterError error) {
                        Log.e(TAG, "Update failed" + error.toString());
                    }
                }).start();
    }

    private void download(Update update, Context context) {
        String version = update.getLatestVersion();
        String filename = "purchaseHistory-" + version + ".apk";
        String uriString = REPO_DOWNLOAD_URL + version + "/" + filename;
        Uri uri = Uri.parse(uriString);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Purchase History Update v" + version);
        request.setDescription("Downloading latest update of PurchaseHistory...");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            Log.i(TAG, "download: " + uriString);
            downloadManager.enqueue(request);
        }
    }
}
