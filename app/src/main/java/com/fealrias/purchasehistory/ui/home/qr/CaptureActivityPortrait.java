package com.fealrias.purchasehistory.ui.home.qr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import com.fealrias.purchasehistory.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

public class CaptureActivityPortrait extends Activity implements CompoundBarcodeView.TorchListener {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageButton switchFlashlightButton;
    private ViewfinderView viewfinderView;
    private boolean isTorchOn = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);
        switchFlashlightButton = findViewById(R.id.switch_flashlight);
        switchFlashlightButton.setContentDescription(getString(R.string.turn_on_flashlight));
        viewfinderView = findViewById(R.id.zxing_viewfinder_view);
        if (!hasFlash()) {
            switchFlashlightButton.setVisibility(View.GONE);
        }
        switchFlashlightButton.setOnClickListener((v) -> {
            if (isTorchOn) {
                barcodeScannerView.setTorchOff();
            } else {
                barcodeScannerView.setTorchOn();
            }
        });
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(false);
        capture.decode();
        changeLaserVisibility(true);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void changeLaserVisibility(boolean visible) {
        viewfinderView.setLaserVisibility(visible);
    }

    @Override
    public void onTorchOn() {
        switchFlashlightButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.flash_off));
        switchFlashlightButton.setContentDescription(getString(R.string.turn_off_flashlight));
        isTorchOn= true;

    }

    @Override
    public void onTorchOff() {
        switchFlashlightButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.flash));
        switchFlashlightButton.setContentDescription(getString(R.string.turn_on_flashlight));
        isTorchOn= false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}