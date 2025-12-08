package com.angelp.purchasehistory;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.angelp.purchasehistory.databinding.ActivityMainBinding;
import com.angelp.purchasehistory.ui.home.HomeActivity;
import com.angelp.purchasehistory.ui.login.LoginActivity;
import com.angelp.purchasehistory.ui.register.RegisterActivity;
import com.angelp.purchasehistory.ui.spectator.SpectatorHomeActivity;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.enums.UserRole;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.bugfender.sdk.Bugfender;
import com.inmobi.sdk.InMobiSdk;
import com.supersuman.apkupdater.ApkUpdater;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    AuthClient authClient;
    private ActivityMainBinding binding;

    private static final String REPO_URL = "https://github.com/Fealrias/PurchaseHistory/releases/latest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(this::startLoginActivity);
        binding.registerButton.setOnClickListener(this::startRegisterActivity);
        updateIfAvailable();
    }


    private void updateIfAvailable() {
        new Thread(() -> {
            ApkUpdater updater = new ApkUpdater(this, REPO_URL);
            updater.setThreeNumbers(true);
            Boolean newUpdateAvailable = updater.isNewUpdateAvailable();
            if (newUpdateAvailable != null && newUpdateAvailable) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.BaseDialogStyle);
                    builder.setTitle(R.string.update_available);
                    builder.setMessage(R.string.update_available_description);
                    builder.setPositiveButton(R.string.download, (dialog, which) -> {
                        updater.requestDownload();
                        redirectIfJwtValid();
                    });
                    builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());
                    builder.setOnDismissListener((d) -> redirectIfJwtValid());
                    builder.show();
                });
            } else {
                redirectIfJwtValid();
            }
        }).start();
    }

    private void redirectIfJwtValid() {
        PurchaseHistoryApplication root = PurchaseHistoryApplication.getInstance();

        String token = root.getUserToken().getValue();
        Log.i("jwtoken", token != null && !token.isEmpty() ? "Found JWT" : "No JWT in phone");
        if (token != null && !token.isEmpty()) {
            binding.loadingMain.setVisibility(View.VISIBLE);
            new Thread(() -> {
                Optional<UserView> loggedUser = Optional.ofNullable(PurchaseHistoryApplication.getInstance().getLoggedUser().getValue());
                try {
                    if (!loggedUser.isPresent()) loggedUser = authClient.getLoggedUser();
                    this.runOnUiThread(() -> binding.loadingMain.setVisibility(View.GONE));
                    if (loggedUser.isPresent()) {
                        Bugfender.setDeviceString("user email", loggedUser.get().getEmail());
                        root.getLoggedUser().postValue(loggedUser.get());
                        if (UserRole.OBSERVER_ROLE.toString().equals(loggedUser.get().getRole()))
                            startSpectatorActivity();
                        else
                            startHomeActivity();
                    } else {
                        runOnUiThread(() -> Toast.makeText(PurchaseHistoryApplication.getContext(), R.string.alert_session_ended, Toast.LENGTH_SHORT).show());
                    }
                } catch (WebException e) {
                    runOnUiThread(() -> {
                        binding.loadingMain.setVisibility(View.GONE);
                        new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                                .setTitle(R.string.login_failed)
                                .setMessage(e.getErrorResource())
                                .show();
                    });
                }
            }).start();
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void startSpectatorActivity() {
        Intent intent = new Intent(this, SpectatorHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void startRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void startLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}