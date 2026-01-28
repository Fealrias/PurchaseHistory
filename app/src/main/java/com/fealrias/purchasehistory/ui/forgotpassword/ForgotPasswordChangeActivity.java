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
import com.fealrias.purchasehistory.ui.login.LoginActivity;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.AuthClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class ForgotPasswordChangeActivity extends AppCompatActivity {

    @Inject
    AuthClient authClient;

    private EditText etCode;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private TextView tvFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_change);

        // Initialize views
        etCode = findViewById(R.id.etCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        tvFeedback = findViewById(R.id.tvFeedback);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar!=null){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        // Button click listener
        btnChangePassword.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInputs(code, newPassword, confirmPassword)) {
                new Thread(() -> attemptPasswordChange(code, newPassword)).start();
            }
        });
    }

    private void attemptPasswordChange(String code, String newPassword) {
        boolean isSuccessful = authClient.changeForgotPassword(code, newPassword);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isSuccessful) {
                tvFeedback.setText(R.string.password_changed_successfully);
                tvFeedback.setTextColor(getColorResource(R.color.successA10));
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }, 3000);
            } else {
                tvFeedback.setText(R.string.failed_to_change_password_please_try_again);
                tvFeedback.setTextColor(getColorResource(R.color.dangerA10));
            }
            tvFeedback.setVisibility(View.VISIBLE);
        });
    }

    private int getColorResource(int color) {
        return getResources().getColor(color, getTheme());
    }

    private boolean validateInputs(String code, String newPassword, String confirmPassword) {
        if (code.length() != 6) {
            showError(getResources().getString(R.string.invalid_code_six));
            return false;
        }
        if (newPassword.isEmpty()) {
            showError(getResources().getString(R.string.invalid_password_empty));
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError(getResources().getString(R.string.invalid_password_match));
            return false;
        }
        if (AndroidUtils.isPasswordInvalid(newPassword)) {
            showError(getResources().getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void showError(String message) {
        tvFeedback.setText(message);
        tvFeedback.setTextColor(getColorResource(R.color.dangerA10));
        tvFeedback.setVisibility(View.VISIBLE);
    }
}
