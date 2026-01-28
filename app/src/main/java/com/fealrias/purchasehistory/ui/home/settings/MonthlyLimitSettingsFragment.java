package com.fealrias.purchasehistory.ui.home.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.SettingsClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MonthlyLimitSettingsFragment extends PreferenceFragmentCompat {
    @Inject
    SettingsClient settingsClient;
    private EditMonthlyLimitDialog editMonthlyLimitDialog;
    private AddMonthlyLimitDialog addMonthlyLimitDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.monthly_limit_preferences, rootKey);

        Preference addLimitPreference = findPreference("add_new_monthly_limit_preference");
        PreferenceCategory monthlyLimitCategory = findPreference("monthly_limit_preference_category");
        setupMonthlyLimitEdit(monthlyLimitCategory);

        if (addLimitPreference != null)
            addLimitPreference.setOnPreferenceClickListener((v) -> {
                addMonthlyLimitDialog = new AddMonthlyLimitDialog((newMonthlyLimit) -> {
                    Preference monthlyLimitPreference = new Preference(requireContext());
                    setupMonthlyLimit(newMonthlyLimit, monthlyLimitPreference);
                    if (monthlyLimitCategory != null)
                        monthlyLimitCategory.addPreference(monthlyLimitPreference);
                });
                addMonthlyLimitDialog.show(getParentFragmentManager(), "Add_monthly_limit");
                return false;
            });


    }

    private void setupMonthlyLimitEdit(PreferenceCategory monthlyLimitCategory) {
        new Thread(() -> {
            List<MonthlyLimitView> monthlyLimits = settingsClient.getMonthlyLimits();
            for (MonthlyLimitView monthlyLimit : monthlyLimits) {
                Preference monthlyLimitPreference = new Preference(requireContext());
                setupMonthlyLimit(monthlyLimit, monthlyLimitPreference);
                monthlyLimitPreference.setOnPreferenceClickListener((p) -> {
                    editMonthlyLimitDialog = new EditMonthlyLimitDialog(monthlyLimit.getId(), monthlyLimit,
                            (newMonthlyLimit) -> {
                                if (newMonthlyLimit == null)
                                    monthlyLimitCategory.removePreference(monthlyLimitPreference);
                                else
                                    setupMonthlyLimit(newMonthlyLimit, monthlyLimitPreference);
                            });
                    editMonthlyLimitDialog.show(getParentFragmentManager(), "Edit_monthly_limit");
                    return false;
                });
                monthlyLimitCategory.addPreference(monthlyLimitPreference);
            }
        }).start();

    }

    private void setupMonthlyLimit(MonthlyLimitView monthlyLimit, Preference monthlyLimitPreference) {

        new Handler(Looper.getMainLooper()).post(() -> {
            monthlyLimitPreference.setTitle(monthlyLimit.getLabel());
            monthlyLimitPreference.setSummary(AndroidUtils.formatCurrency(monthlyLimit.getValue()));
        });
    }
}