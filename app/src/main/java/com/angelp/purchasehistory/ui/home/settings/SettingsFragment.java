package com.angelp.purchasehistory.ui.home.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.ui.feedback.FeedbackActivity;
import com.angelp.purchasehistory.ui.legal.AboutUsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.UserClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;

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
        Preference creditsPreference = findPreference("credits_preference");
        creditsPreference.setOnPreferenceClickListener((p) -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
            return false;
        });
        ListPreference currencyPreference = findPreference("currency_preference");
        MutableLiveData<UserView> loggedUser = PurchaseHistoryApplication.getInstance().loggedUser;
        currencyPreference.setValue(loggedUser.getValue().getPreferredCurrency());
        currencyPreference.setOnPreferenceChangeListener((a, value) -> {
            new Thread(() -> {
                UserView userView = userClient.updatePreferredCurrency(value.toString());
                loggedUser.postValue(userView);
            }).start();
            return false;
        });
        Preference monthlyLimitPreference = findPreference("monthly_limit_preference");
        monthlyLimitPreference.setOnPreferenceClickListener((p) -> navigate(MonthlyLimitSettingsFragment.class));
        Preference reportProblemPreference = findPreference("report_problem");
        reportProblemPreference.setOnPreferenceClickListener((p) -> {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
            return false;
        });
        ListPreference languagePreference = findPreference("language_preference");
        if (languagePreference != null) {
            // Set current language value from app preferences or default
            SharedPreferences appPreferences = requireContext().getSharedPreferences(Constants.Preferences.APP_PREFERENCES, Context.MODE_PRIVATE);
            String currentLang = appPreferences.getString(Constants.Preferences.PREFERRED_LANGUAGE, "en");
            languagePreference.setValue(currentLang);

            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectedLang = newValue.toString();
                appPreferences.edit().putString(Constants.Preferences.PREFERRED_LANGUAGE, selectedLang).apply();
                AndroidUtils.setLocale(selectedLang);
                return true;
            });
        }
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