package com.angelp.purchasehistory.ui.home.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.ui.feedback.FeedbackActivity;
import com.angelp.purchasehistory.ui.legal.AboutUsActivity;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.UserClient;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = getClass().getName();
    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.application_settings);
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        loadAppSettings();
    }

    private void loadAppSettings() {
        SharedPreferences appPreferences = getContext().getSharedPreferences(Constants.Preferences.APP_PREFERENCES, Context.MODE_PRIVATE);
        String preferredCurrency = appPreferences.getString(Constants.Preferences.PREFERRED_CURRENCY, "");
        Preference creditsPreference = findPreference("credits_preference");
        creditsPreference.setOnPreferenceClickListener((p)-> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
            return false;
        });
        ListPreference currencyPreference = findPreference("currency_preference");
        currencyPreference.setValue(preferredCurrency);
        currencyPreference.setOnPreferenceChangeListener((a, value) -> appPreferences.edit()
                .putString(Constants.Preferences.PREFERRED_CURRENCY, value.toString()).commit());
        Preference monthlyLimitPreference = findPreference("monthly_limit_preference");
        monthlyLimitPreference.setOnPreferenceClickListener((p) -> navigate(MonthlyLimitSettingsFragment.class));
        Preference reportProblemPreference = findPreference("report_problem");
        reportProblemPreference.setOnPreferenceClickListener((p) -> {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
            return false;
        });
    }

    private boolean navigate(Class<? extends Fragment> fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment, null)
                .addToBackStack("application_settings")
                .commit();
        return true;
    }
}