package com.fealrias.purchasehistory.ui.forgotpassword;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.web.clients.AuthClient;
import com.fealrias.purchasehistory.web.clients.WebException;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class ForgotPasswordEmailActivity extends AppCompatActivity {

    @Inject
    AuthClient authClient;

    private EditText etEmail;
    private Button btnSendEmail;
    private TextView tvFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        tvFeedback = findViewById(R.id.tvFeedback);
        Button btnAlreadyHaveCode = findViewById(R.id.btnAlreadyHaveCode);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar!=null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        btnAlreadyHaveCode.setOnClickListener(v -> startPasswordChangeActivity());
        btnSendEmail.setOnClickListener(v -> {
            btnSendEmail.setEnabled(false);
            tvFeedback.setTextColor(getColorResource(R.color.surfaceA10));
            tvFeedback.setText(R.string.sending);
            String email = etEmail.getText().toString().trim();
            if (isValidEmail(email)) {
                new Thread(() -> sendEmail(email)).start();
                tvFeedback.setVisibility(View.VISIBLE);
            } else {
                btnSendEmail.setEnabled(true);
                tvFeedback.setText(R.string.please_enter_a_valid_email_address);
                tvFeedback.setTextColor(getColorResource(R.color.dangerA10));
                tvFeedback.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendEmail(String email) {
        try {
            boolean isSuccessful = authClient.forgotPassword(email);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isSuccessful) {
                    tvFeedback.setText(R.string.reset_email_sent_successfully);
                    tvFeedback.setTextColor(getColorResource(R.color.successA10));
                    new Handler(Looper.getMainLooper()).postDelayed(this::startPasswordChangeActivity, 3000);
                } else {
                    btnSendEmail.setEnabled(true);
                    tvFeedback.setText(R.string.failed_to_send_reset_email_please_try_again);
                    tvFeedback.setTextColor(getColorResource(R.color.dangerA10));
                }
            });
        } catch (WebException e) {
            new Handler(Looper.getMainLooper()).post(() -> {
                tvFeedback.setTextColor(getColorResource(R.color.dangerA10));
                tvFeedback.setText(e.getErrorResource());
                btnSendEmail.setEnabled(!e.getErrorResource().equals(R.string.tooManyRequest_429));
            });
        }
    }

    private int getColorResource(int color) {
        return getResources().getColor(color, getTheme());
    }

    private void startPasswordChangeActivity() {
        btnSendEmail.setEnabled(true);
        Intent intent = new Intent(this, ForgotPasswordChangeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}