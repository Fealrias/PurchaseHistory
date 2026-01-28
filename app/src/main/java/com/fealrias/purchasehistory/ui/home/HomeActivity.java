package com.fealrias.purchasehistory.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.data.tour.TourStep;
import com.fealrias.purchasehistory.databinding.ActivityHomeBinding;
import com.fealrias.purchasehistory.ui.home.dashboard.CustomizationDialogFragment;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.AuthClient;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.NonNull;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    @Inject
    PurchaseFilterSingleton filter;
    private ActivityHomeBinding binding;
    private int tourStep = 0;
    private NavController navController;
    private final Gson gson = new Gson();

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_qrscanner, R.id.navigation_scheduled_expenses, R.id.navigation_profile)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user_activity);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener((controller, currentDest, c) -> invalidateOptionsMenu());
            NavigationUI.setupWithNavController(binding.navView, navController, false);
            NavigationUI.setupActionBarWithNavController(this, navHostFragment.getNavController(), appBarConfiguration);
        }
        if (AndroidUtils.isFirstTimeOpen(this)) {
            showTourPrompt();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void showTourPrompt() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.tour_guide_bubble);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button yesButton = dialog.findViewById(R.id.tour_guide_next);
        yesButton.setOnClickListener(v -> {
            binding.navView.setEnabled(false);
            findViewById(R.id.filter_btn).setEnabled(false);
            showNextTourStep();
            dialog.dismiss();
        });

        Button skipButton = dialog.findViewById(R.id.tour_guide_skip);
        skipButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(Constants.Preferences.APP_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, false);
            editor.apply();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showNextTourStep() {
        if (tourStep < Constants.tourSteps.size()) {
            TourStep step = Constants.tourSteps.get(tourStep);
            new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(findViewById(step.getId()))
                    .setPrimaryText(step.getPrimaryText())
                    .setSecondaryText(step.getSecondaryText())
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                            tourStep++;
                            showNextTourStep();
                        }
                    })
                    .show();
        } else {
            binding.navView.setEnabled(true);
            SharedPreferences preferences = getSharedPreferences(Constants.Preferences.APP_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, false);
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem dashboardEditButton = menu.findItem(R.id.menu_dashboard_settings);
        if (navController != null && navController.getCurrentDestination() != null)
            dashboardEditButton.setVisible(Objects.equals(navController.getCurrentDestination().getId(), R.id.navigation_dashboard));
        return super.onPrepareOptionsMenu(menu);

    }

    private String getCurrentHelpPrompt(NavDestination currentDestination) {
        int id = currentDestination.getId();
        if (Objects.equals(id, R.id.navigation_dashboard))
            return getString(R.string.help_dashboard);
        if (Objects.equals(id, R.id.navigation_qrscanner))
            return getString(R.string.help_qrscanner);
        if (Objects.equals(id, R.id.navigation_profile))
            return getString(R.string.help_profile);
        if (Objects.equals(id, R.id.navigation_scheduled_expenses))
            return getString(R.string.help_schedule);
        return "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menu_dashboard_settings) {
            openCustomizationDialog();
        } else if (itemId == R.id.menu_help && navController != null) {
            showHelp(navController.getCurrentDestination());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp(NavDestination currentDestination) {
        String currentHelpPrompt = getCurrentHelpPrompt(currentDestination);
        new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                .setMessage(currentHelpPrompt)
                .create().show();
    }

    private void openCustomizationDialog() {
        List<DashboardComponent> fragments = getFragmentsFromPreferences();
        CustomizationDialogFragment dialog = new CustomizationDialogFragment(fragments, updatedFragments -> {

            saveFragmentsSetupToPreferences(updatedFragments);
            Fragment dashboard = binding.navHostFragmentUserActivity.getFragment();
            if (dashboard != null) {
                getSupportFragmentManager().beginTransaction().detach(dashboard).commit();
                getSupportFragmentManager().beginTransaction().attach(dashboard).commit();
            }
        });
        dialog.show(getSupportFragmentManager(), "customizationDialog");
    }

    private List<DashboardComponent> getFragmentsFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
        String savedFragmentsJson = preferences.getString("saved_fragments", "[]");
        Type type = new TypeToken<List<DashboardComponent>>() {
        }.getType();

        List<DashboardComponent> dashboardComponents = gson.fromJson(savedFragmentsJson, type);
        List<DashboardComponent> allComponents = new ArrayList<>(Constants.DEFAULT_COMPONENTS);
        for (DashboardComponent all : allComponents) {
            if (!dashboardComponents.contains(all)) dashboardComponents.add(all);
        }
        return dashboardComponents.stream().map(DashboardComponent::fillFromName).collect(Collectors.toList());
    }

    private void saveFragmentsSetupToPreferences(List<DashboardComponent> selectedFragments) {
        SharedPreferences preferences = PurchaseHistoryApplication.getContext()
                .getSharedPreferences(Constants.Preferences.DASHBOARD_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String fragmentsJson = gson.toJson(selectedFragments);
        editor.remove("saved_fragments");
        editor.putString("saved_fragments", fragmentsJson);
        editor.apply();
    }
}