package com.fealrias.purchasehistory.ui.legal;

import android.os.Bundle;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.databinding.ActivityTermsAndConditionsBinding;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private ActivityTermsAndConditionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.termsAndConditionsWebview.setWebViewClient(new WebViewClient());
        binding.termsAndConditionsWebview.loadUrl("file:///android_asset/terms_and_conditions.html");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}