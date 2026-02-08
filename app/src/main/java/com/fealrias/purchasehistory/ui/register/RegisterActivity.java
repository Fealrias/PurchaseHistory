package com.fealrias.purchasehistory.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.ActivityRegisterBinding;
import com.fealrias.purchasehistory.ui.legal.PrivacyPolicyActivity;
import com.fealrias.purchasehistory.ui.legal.TermsAndConditionsActivity;
import com.fealrias.purchasehistory.ui.login.LoginActivity;
import com.fealrias.purchasehistory.util.AfterTextChangedWatcher;
import com.fealrias.purchasehistory.web.clients.WebException;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import com.google.android.recaptcha.Recaptcha;
import com.google.android.recaptcha.RecaptchaAction;
import com.google.android.recaptcha.RecaptchaTasksClient;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private ActivityRegisterBinding binding;
    private RecaptchaTasksClient recaptchaTasksClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        initializeRecaptchaClient();
        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText confirmPassword = binding.confirmPassword;
        final EditText emailEditText = binding.email;
        final Button registerButton = binding.registerRegisterButton;
        final ProgressBar loadingProgressBar = binding.loading;
        final TextView errorLabel = binding.errorLabel;
        final CheckBox acceptTermsCheckBox = binding.acceptTerms;

        binding.registerBackButton.setOnClickListener((view) -> onBackPressed());

        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            registerButton.setEnabled(registerFormState.isDataValid() && acceptTermsCheckBox.isChecked());
            if (registerFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registerFormState.getUsernameError()));
            }
            if (registerFormState.getEmailError() != null) {
                emailEditText.setError(getString(registerFormState.getEmailError()));
            }
            if (registerFormState.getPasswordError() != null) {
                if (registerFormState.getPasswordError().equals(R.string.invalid_password_match))
                    confirmPassword.setError(getString(registerFormState.getPasswordError()));
                else passwordEditText.setError(getString(registerFormState.getPasswordError()));
            }
        });

        registerViewModel.getRegisterResult().observe(this, registerResult -> {
            if (registerResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (registerResult.getError() != null) {
                showRegisterFailed(registerResult.getError());
            }
            if (registerResult.getSuccess() != null) {
                binding.errorLabel.setVisibility(View.INVISIBLE);
                updateUiWithUser(registerResult.getSuccess());
                setResult(Activity.RESULT_OK);
                Intent toLogin = new Intent(this, LoginActivity.class);
                toLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);

                startActivity(toLogin );
                finish();
            }
        });

        checkIfLoggedIn();

        TextWatcher afterTextChangedListener = new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(usernameEditText.getText().toString().trim(), passwordEditText.getText().toString().trim(), confirmPassword.getText().toString().trim(), emailEditText.getText().toString().trim());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);

        acceptTermsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean dataValid = registerViewModel.getRegisterFormState().getValue() != null && registerViewModel.getRegisterFormState().getValue().isDataValid();
            registerButton.setEnabled(dataValid && isChecked);
        });

        emailEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                register(usernameEditText, passwordEditText, emailEditText);
            }
            return false;
        });

        registerButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            register(usernameEditText, passwordEditText, emailEditText);
        });
        binding.termsLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        binding.privacyLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    private void initializeRecaptchaClient() {
        Recaptcha.fetchTaskClient(PurchaseHistoryApplication.getInstance(), "6Lcg2SAsAAAAAOmgmnY1hVNs6iyZa2uPJLIXibxK")
                .addOnSuccessListener(this, client -> recaptchaTasksClient = client)
                .addOnFailureListener(this, e -> Log.e("RECAPTCHA", "initializeRecaptchaClient: Fail error:" + e.getMessage()));
    }

    private void checkIfLoggedIn() {
        new Thread(registerViewModel.checkIfLoggedIn(), "CheckLogin").start();
    }

    private void register(EditText usernameEditText, EditText passwordEditText, EditText emailEditText) {
        LocaleList locales = getResources().getConfiguration().getLocales();
        Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
        if (recaptchaTasksClient != null) {
            recaptchaTasksClient.executeTask(RecaptchaAction.SIGNUP).addOnSuccessListener(this, token -> {
                try {
                    new Thread(registerViewModel.register(usernameEditText.getText().toString().trim(), passwordEditText.getText().toString().trim(), emailEditText.getText().toString().trim(), locale, token), "Register").start();
                } catch (WebException e) {
                    showRegisterFailed(e.getErrorResource());
                }
            }).addOnFailureListener(this, e -> Toast.makeText(getApplicationContext(), R.string.captcha_fail, Toast.LENGTH_LONG).show());
        } else {
            showRegisterFailed(R.string.captcha_fail);
        }
    }

    private void updateUiWithUser(UserView ignoredModel) {
        String welcome = getString(R.string.account_created);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        binding.errorLabel.setText(errorString);
        binding.errorLabel.setVisibility(View.VISIBLE);
        Animation jump = AnimationUtils.loadAnimation(this, R.anim.jump);
        binding.errorLabel.startAnimation(jump);
    }


}