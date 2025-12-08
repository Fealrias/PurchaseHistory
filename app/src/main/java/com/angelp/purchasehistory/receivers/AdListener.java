package com.angelp.purchasehistory.receivers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;

public class AdListener extends BannerAdEventListener {
    private static final String TAG = "QR_SCANNER_BANNER_AD";

    public AdListener() {
        super();
        Log.i(TAG, "AdListener: created");
    }

    /**
     * @param inMobiBanner
     * @param inMobiAdRequestStatus
     */
    @Override
    public void onAdFetchFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
        super.onAdFetchFailed(inMobiBanner, inMobiAdRequestStatus);
        Log.i(TAG, "AdListener: Fetch FAILED");

    }

    /**
     * @param inMobiBanner
     */
    @Override
    public void onAdDisplayed(@NonNull InMobiBanner inMobiBanner) {
        super.onAdDisplayed(inMobiBanner);
        Log.i(TAG, "AdListener: AD Displayed successfully");

    }

    /**
     * @param inMobiBanner
     */
    @Override
    public void onAdDismissed(@NonNull InMobiBanner inMobiBanner) {
        super.onAdDismissed(inMobiBanner);
        Log.i(TAG, "AdListener: AD DISMISSED");

    }
}
