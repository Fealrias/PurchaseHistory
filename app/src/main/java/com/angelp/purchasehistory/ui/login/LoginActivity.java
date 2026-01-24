package com.angelp.purchasehistory.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.angelp.purchasehistory.databinding.ActivityLoginBinding;
import com.angelp.purchasehistory.receivers.scheduled.NotificationHelper;
import com.angelp.purchasehistory.ui.forgotpassword.ForgotPasswordEmailActivity;
import com.angelp.purchasehistory.ui.home.HomeActivity;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.ScheduledExpenseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.NonNull;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;
    @Inject
    PurchaseFilterSingleton purchaseFilterSingleton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);
        checkIfLoggedIn();
        binding.backButton.setOnClickListener((view) -> onBackPressed());
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            binding.loading.setVisibility(View.GONE);
            binding.loginLoginButton.setEnabled(true);
            if (loginResult.getSuccess() == null) {
                showLoginFailed(loginResult.getError());
            } else {
                scheduleNotificationsFromUser(this);
                purchaseFilterSingleton.updateFilter(Constants.getDefaultFilter());
                updateUiWithUser(loginResult.getSuccess().getUsername());
                setResult(Activity.RESULT_OK);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        TextWatcher afterTextChangedListener = new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                onFormChange();
            }
        };
        binding.username.addTextChangedListener(afterTextChangedListener);
        binding.password.addTextChangedListener(afterTextChangedListener);
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin(binding.username, binding.password);
            }
            return false;
        });

        binding.loginLoginButton.setOnClickListener(v -> {
            binding.loading.setVisibility(View.VISIBLE);
            binding.loginLoginButton.setEnabled(false);
            attemptLogin(binding.username, binding.password);
        });
        binding.forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordEmailActivity.class);
            startActivity(intent);
        });
        AutofillManager afm = getSystemService(AutofillManager.class);

        afm.registerCallback(new AutofillManager.AutofillCallback() {
            @Override
            public void onAutofillEvent(@NonNull View view, int event) {
                super.onAutofillEvent(view, event);
                onFormChange();
            }
        });
//        binding.googleSignInButton.setOnClickListener((v)-> loginWithGoogle());
    }


    private void scheduleNotificationsFromUser(Context context) {
        new Thread(() -> {
            List<ScheduledExpenseView> all = scheduledExpenseClient.findAllForUser();
            if (all.isEmpty()) return;
            NotificationHelper.setupAllAlarms(context, all);
        }).start();
    }

    private void checkIfLoggedIn() {
        new Thread(loginViewModel.checkIfLoggedIn(), "CheckLogin").start();
    }

    private void attemptLogin(EditText usernameEditText, EditText passwordEditText) {
        new Thread(loginViewModel.login(usernameEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim()), "Login").start();
    }

    private void updateUiWithUser(String username) {
        String welcome = String.format(getString(R.string.welcome), username);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                .setTitle(R.string.login_failed)
                .setMessage(errorString).create().show();
    }

    private void onFormChange() {
        String username = binding.username.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        boolean isValid = validate(username, password);
        binding.loginLoginButton.setEnabled(isValid);
    }

    private boolean validate(String username, String password) {
        if (AndroidUtils.isUserNameInvalid(username)) {
            binding.username.setError(getString(R.string.invalid_username));
            return false;
        }
        if (AndroidUtils.isPasswordInvalid(password)) {
            binding.username.setError(null);
            binding.password.setError(getString(R.string.invalid_password));
            return false;
        }
        binding.password.setError(null);
        return true;
    }
}


//    private void loginWithGoogle() {
//    }
