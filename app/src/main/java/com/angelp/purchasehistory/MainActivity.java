package com.angelp.purchasehistory;

import android.content.Intent;
import android.os.Bundle;
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
import com.angelp.purchasehistory.web.clients.VersionUpdater;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.enums.UserRole;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    AuthClient authClient;
    @Inject
    VersionUpdater versionUpdater;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.loginButton.setOnClickListener(this::startLoginActivity);
        binding.registerButton.setOnClickListener(this::startRegisterActivity);
        versionUpdater.checkForUpdate(this, ()-> runOnUiThread(this::redirectIfJwtValid));
    }

    private void redirectIfJwtValid() {
        PurchaseHistoryApplication root = PurchaseHistoryApplication.getInstance();

        String token = root.getUserToken().getValue();
        Log.i("jwtoken", token != null && !token.isEmpty() ? "Found JWT" : "No JWT in phone");
        if (token != null && !token.isEmpty()) {
            runOnUiThread(() -> binding.loadingMain.setVisibility(View.VISIBLE));
            new Thread(() -> {
                Optional<UserView> loggedUser = Optional.ofNullable(PurchaseHistoryApplication.getInstance().getLoggedUser().getValue());
                try {
                    if (!loggedUser.isPresent()) loggedUser = authClient.getLoggedUser();
                    this.runOnUiThread(() -> binding.loadingMain.setVisibility(View.GONE));
                    if (loggedUser.isPresent()) {
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