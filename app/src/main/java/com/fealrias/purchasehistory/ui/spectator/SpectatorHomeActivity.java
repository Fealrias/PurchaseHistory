package com.fealrias.purchasehistory.ui.spectator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.ActivitySpectatorBinding;
import com.fealrias.purchasehistory.ui.home.settings.SettingsActivity;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.AuthClient;
import com.fealrias.purchasehistory.web.clients.ObserverClient;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class SpectatorHomeActivity extends AppCompatActivity {
    @Inject
    ObserverClient observerClient;
    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    private ActivitySpectatorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpectatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureNavigation();
    }

    private void configureNavigation() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_spectator_dashboard, R.id.navigation_spectated_users)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_spectator_activity);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.spectatorNavView, navController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spectator_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.menu_item_logout) {
            new Thread(() -> {
                authClient.logout();
                AndroidUtils.logout(this, purchaseClient);
            }).start();
        }
        return false;
    }
}