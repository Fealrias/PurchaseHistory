package com.fealrias.purchasehistory.ui.legal;

import android.os.Bundle;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.privacyPolicyWebview.setWebViewClient(new WebViewClient());
        binding.privacyPolicyWebview.loadUrl("file:///android_asset/privacy_policy.html");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}