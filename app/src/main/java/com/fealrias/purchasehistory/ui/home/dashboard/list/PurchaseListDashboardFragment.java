package com.fealrias.purchasehistory.ui.home.dashboard.list;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.databinding.FragmentPurchasesListCardBinding;
import com.fealrias.purchasehistory.ui.FullscreenGraphActivity;
import com.fealrias.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.fealrias.purchasehistorybackend.models.views.outgoing.analytics.PurchaseListView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PurchaseListDashboardFragment extends RefreshablePurchaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    private FragmentPurchasesListCardBinding binding;
    private PurchasesAdapter purchasesAdapter;
    private boolean showFilter;
    private int maxSize;
    private PurchaseFilter localFilter;

    public PurchaseListDashboardFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        initFilterRow();
        PurchaseFilter filterValue = localFilter == null ? filterViewModel.getFilterValue() : localFilter;
        this.applyFilter(filterValue);
        initializePurchasesRecyclerView(maxSize, filterValue);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchasesListCardBinding.inflate(inflater, container, false);
        maxSize = -1;
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
            localFilter = getArguments().getParcelable(Constants.Arguments.ARG_FILTER);
            maxSize = getArguments().getInt(Constants.Arguments.ARG_MAX_SIZE);
        }
        super.setLoadingScreen(binding.loadingBar);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializePurchasesRecyclerView(int maxSize, PurchaseFilter filter) {
        new Thread(() -> {
            PurchaseListView purchaseListView = purchaseClient.getAllPurchases(filter);
            List<PurchaseView> purchases = purchaseListView.getContent();
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity());
            setupShowMoreButton(purchases.size(), maxSize);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding != null) {
                    binding.emptyView.getRoot().setVisibility(purchases.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.emptyView.addNewPurchaseButton.setOnClickListener((v) -> NavHostFragment.findNavController(this).navigate(R.id.navigation_qrscanner, new Bundle()));
                    binding.purchaseSumText.setText(AndroidUtils.formatCurrency(purchaseListView.getTotalSum()));
                    binding.purchaseList.setLayoutManager(llm);
                    binding.purchaseList.setAdapter(purchasesAdapter);
                }
            });
        }).start();
    }

    private void setShowEmptyView(boolean empty) {
        binding.emptyView.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void setupShowMoreButton(int purchaseSize, int maxSize) {
        if (binding == null) return;
        binding.seeAllButton.setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            DashboardComponent dashboardComponent = new DashboardComponent("PurchaseListPurchaseFragment");
            intent.putExtra(Constants.Arguments.ARG_COMPONENT, dashboardComponent);
            startActivity(intent);
        });
        updateSeeAllButton(purchaseSize, maxSize);
    }

    private void updateSeeAllButton(int purchaseSize, int maxSize) {
        purchasesAdapter.setLimit(maxSize);
        boolean isBiggerThanLimit = maxSize > 0 && maxSize < purchaseSize;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (binding == null) return;
            binding.seeAllButton.setText(getString(R.string.see_all_n_purchases, purchaseSize));
            binding.seeAllButton.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
            binding.seeAllBackdrop.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
        });
    }

    public void refresh(PurchaseFilter mainFilter) {
        PurchaseFilter filter = localFilter == null ? mainFilter : localFilter;
        if (purchasesAdapter == null || binding == null) {
            Log.w(TAG, "refresh: Purchases adapter is missing. Skipping refresh");
            return;
        }
        isRefreshing.postValue(true);
        new Thread(() -> {
            PurchaseListView purchaseListView = purchaseClient.getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + purchaseListView.getContent().size());
            updateAdapter(purchaseListView);
            isRefreshing.postValue(false);
        }).start();
    }

    private void updateAdapter(PurchaseListView allPurchases) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (binding == null) return;
            binding.emptyView.getRoot().setVisibility(allPurchases.getContent().isEmpty() ? View.VISIBLE : View.GONE);
            purchasesAdapter.setPurchaseViews(allPurchases.getContent());
            binding.purchaseSumText.setText(AndroidUtils.formatCurrency(allPurchases.getTotalSum()));
            updateSeeAllButton(allPurchases.getContent().size(), maxSize);
        });
    }

    private void initFilterRow() {
        binding.filterDateText.setTextColor(getContext().getColor(R.color.text));
        new Handler(Looper.getMainLooper()).post(() ->
        {
            if (binding == null) return;
            binding.filterRow.setVisibility(showFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void applyFilter(PurchaseFilter newFilter) {
        new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null) return;
                    binding.filterDateText.setText(newFilter.getDateString());
                }
        );
    }
}