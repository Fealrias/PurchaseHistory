package com.fealrias.purchasehistory.ui.home.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.databinding.ActivitySettingsBinding;
import com.fealrias.purchasehistory.ui.home.HomeActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.arrow_turn_left);
        }
        if (savedInstanceState == null) {
            String fragmentName = intent.getStringExtra("fragment_name");
            if (Constants.SettingsLocations.EDIT_MONTHLY_LIMIT.equals(fragmentName)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.settingsContainer.getId(), new MonthlyLimitSettingsFragment())
                        .commit();
            } else if (Constants.SettingsLocations.EDIT_CATEGORY.equals(fragmentName)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.settingsContainer.getId(), new CategorySettingsFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(binding.settingsContainer.getId(), SettingsFragment.class, null)
                        .commit();
            }
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPress();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backPress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backPress() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i(TAG, "popping backstack");
            fm.popBackStack();
        } else {
            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
        }
    }
}