package com.fealrias.purchasehistory.ui.home.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.databinding.FragmentDashboardBinding;
import com.fealrias.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.analytics.PurchaseListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AndroidEntryPoint
public class DashboardFragment extends RefreshablePurchaseFragment implements CustomizableDashboard {
    private final String TAG = this.getClass().getSimpleName();
    private final Gson gson = new Gson();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;

    @Inject
    PurchaseClient client;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        super.setLoadingScreen(binding.loadingBar);
        super.setDataToHide(binding.dashboardDataView);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeDashboardFragments();
        filterDialog = new PurchaseFilterDialog(true);
        binding.filterBar.filterBtn.setOnClickListener(v -> openFilter());
        binding.empty.addNewPurchaseButton.setOnClickListener((v) -> NavHostFragment.findNavController(this).navigate(R.id.navigation_qrscanner, new Bundle()));
        binding.dashboardScanQr.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.Arguments.OPEN_CAMERA, true);
            NavHostFragment.findNavController(this).navigate(R.id.navigation_qrscanner, bundle);
        });

//        binding.customizeDashboardButton.setOnClickListener(v -> openCustomizationDialog());
    }

    private void initializeDashboardFragments() {
        new Thread(() -> {
            PurchaseListView allPurchases = client.getAllPurchases(super.filterViewModel.getFilterValue());
            List<DashboardComponent> savedFragments = getFragmentsFromPreferences();
            if (savedFragments == null || savedFragments.isEmpty()) {
                savedFragments = new ArrayList<>(Constants.DEFAULT_COMPONENTS);
            } else for (DashboardComponent defaultComponent : Constants.DEFAULT_COMPONENTS) {
                if (!savedFragments.contains(defaultComponent)) {
                    defaultComponent.setVisible(false);
                    savedFragments.add(defaultComponent);
                } // upon application update, the saved fragments might not contain the new default components
            }
            FragmentTransaction fragmentTransaction = setupFragments(savedFragments, savedFragments);
            new Handler(Looper.getMainLooper()).post(() -> {
                setEmptyState(allPurchases);
                fragmentTransaction.commitNow();
                isRefreshing.postValue(false);
            });
        }).start();
    }

    private List<DashboardComponent> getFragmentsFromPreferences() {
        if (getActivity() == null) return null;
        SharedPreferences preferences = getActivity().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
        String savedFragmentsJson = preferences.getString("saved_fragments", "[]");
        Type type = new TypeToken<List<DashboardComponent>>() {
        }.getType();

        List<DashboardComponent> dashboardComponents = gson.fromJson(savedFragmentsJson, type);
        return dashboardComponents.stream().map(DashboardComponent::fillFromName).collect(Collectors.toList());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!binding.filterBar.filterBtn.isEnabled()) binding.filterBar.filterBtn.setEnabled(true);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        int color = newFilter.getCategoryId() == null ? getResources().getColor(R.color.surfaceA20, requireContext().getTheme()) : AndroidUtils.getColor(newFilter.getCategoryColor());
        AndroidUtils.tint(binding.filterBar.filterCategoryBtn, color);
        binding.filterBar.filterCategoryBtn.setTextColor(AndroidUtils.getTextColor(color));
        binding.filterBar.filterCategoryBtn.setText(newFilter.getCategoryName() == null ? getString(R.string.category) : newFilter.getCategoryName());
        binding.filterBar.filterDateBtn.setText(newFilter.getDateString());
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        isRefreshing.setValue(true);
        applyFilter(filter);
        new Thread(() -> {
            PurchaseListView allPurchases = client.getAllPurchases(filter);
            new Handler(Looper.getMainLooper()).post(() -> setEmptyState(allPurchases));
            isRefreshing.postValue(false);
        }).start();

    }

    private void setEmptyState(PurchaseListView content) {
        boolean isEmpty = content == null || content.getContent().isEmpty();
        binding.dashboardFragmentsLinearLayout.setVisibility(isEmpty ? View.GONE: View.VISIBLE);
        binding.empty.getRoot().setVisibility(isEmpty ? View.VISIBLE: View.GONE);
    }


    private FragmentTransaction setupFragments(List<DashboardComponent> fragments, List<DashboardComponent> newFragments) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = getParentFragmentManager().findFragmentByTag("dashboardFragment" + i);
            if (fragment != null)
                transaction.remove(fragment);
        }
            for (int i = 0; i < newFragments.size(); i++) {
                DashboardComponent selectedFragment = newFragments.get(i);
                if (selectedFragment.isVisible()) {
                    DashboardCardFragment dashboardCardFragment = new DashboardCardFragment(selectedFragment);
                    if (i == newFragments.size() - 1 && dashboardCardFragment.getArguments() != null) {
                        dashboardCardFragment.getArguments().putInt("marginBottom", 200);
                    }
                    transaction.add(binding.dashboardFragmentsLinearLayout.getId(), dashboardCardFragment, "dashboardFragment" + i);
                }
            }

        return transaction;
    }
}