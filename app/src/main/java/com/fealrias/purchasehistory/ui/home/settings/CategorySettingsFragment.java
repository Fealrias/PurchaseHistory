package com.fealrias.purchasehistory.ui.home.settings;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.components.form.EditCategoryDialog;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class CategorySettingsFragment extends PreferenceFragmentCompat {

    @Inject
    PurchaseClient purchaseClient;
    private EditCategoryDialog editCategoryDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.category_preferences, rootKey);
        PreferenceCategory category = findPreference("categories_preference_category");
        setupCategoryEdit(category);
    }

    private void setupCategoryEdit(PreferenceCategory category) {
        new Thread(() -> {
            List<CategoryView> allCategories = purchaseClient.getAllCategories();
            for (CategoryView categoryView : allCategories) {
                Preference categoryPreference = new Preference(requireContext());
                setupCategory(categoryView, categoryPreference);
                categoryPreference.setOnPreferenceClickListener((p) -> {
                    editCategoryDialog = new EditCategoryDialog(categoryView.getId(), categoryView,
                            (newCategory) -> setupCategory(newCategory, categoryPreference),
                            () -> {
                                editCategoryDialog.dismiss();
                                requireActivity().runOnUiThread(() -> category.removePreference(categoryPreference));
                            });
                    editCategoryDialog.show(getParentFragmentManager(), "Edit_category");
                    return false;
                });
                category.addPreference(categoryPreference);
            }
        }).start();
    }

    private void setupCategory(CategoryView category, Preference categoryPreference) {
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.circle);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, AndroidUtils.getColor(category));
        new Handler(Looper.getMainLooper()).post(() -> {
            categoryPreference.setTitle(category.getName());
            categoryPreference.setSummary(category.getColor());
            categoryPreference.setIcon(wrappedDrawable);
        });
    }


}