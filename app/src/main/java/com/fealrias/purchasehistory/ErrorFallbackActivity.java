package com.fealrias.purchasehistory;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.web.clients.UserClient;
import com.fealrias.purchasehistorybackend.models.views.incoming.ErrorFeedback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ErrorFallbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_fallback);
        setTitle(R.string.title_error_fallback);
        String errorDetails = getIntent().getStringExtra("error_details");

        TextView errorTextView = findViewById(R.id.error_text_view);
        errorTextView.setText(errorDetails);

        FloatingActionButton copyButton = findViewById(R.id.copyContentButton);
        copyButton.setOnClickListener((v) -> onCopyPressed(errorDetails));

        Button sendFeedbackButton = findViewById(R.id.send_feedback_button);
        sendFeedbackButton.setOnClickListener(v -> sendFeedback(errorDetails));

        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finishAndRemoveTask());
    }

    private void sendFeedback(String errorDetails) {
        UserClient userClient = new UserClient();
        new Thread(() -> {
            ErrorFeedback errorFeedback = new ErrorFeedback(errorDetails, "PurchaseHistory Error Feedback", "error");
            userClient.sendFeedback(errorFeedback);
            finishAndRemoveTask();
        }).start();
    }

    private void onCopyPressed(String errorDetails) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Exception", errorDetails);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.content_copied, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }
}