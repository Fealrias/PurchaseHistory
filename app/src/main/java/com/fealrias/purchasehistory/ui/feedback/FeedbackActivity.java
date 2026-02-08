package com.fealrias.purchasehistory.ui.feedback;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.ActivityFeedbackBinding;
import com.fealrias.purchasehistory.web.clients.UserClient;
import com.fealrias.purchasehistorybackend.models.views.incoming.ErrorFeedback;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class FeedbackActivity extends AppCompatActivity {
    @Inject
    UserClient userClient;
    private ActivityFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        binding.closeButton.setOnClickListener(v -> finish());
        binding.sendFeedbackButton.setOnClickListener(v -> {
            String errorDetails = binding.largeMultilineTextView.getText().toString();
            String title = binding.titleEditText.getText().toString();
            if (errorDetails.isEmpty() || title.isEmpty()) {
                if (errorDetails.isEmpty()) {
                    binding.largeMultilineTextView.setError(getString(R.string.error_required_field));
                }
                if (title.isEmpty()) {
                    binding.titleEditText.setError(getString(R.string.error_required_field));
                }
            } else {
                sendFeedback(errorDetails, title);
            }
        });
        setContentView(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void sendFeedback(String errorDetails, String title) {
        new Thread(() -> {
            ErrorFeedback errorFeedback = new ErrorFeedback(errorDetails, title, "feedback");
            userClient.sendFeedback(errorFeedback);
            finish();
        }).start();
    }
}