package com.fealrias.purchasehistory.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.factories.DashboardComponentsFactory;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.databinding.ActivityFullscreenGraphBinding;
import com.fealrias.purchasehistory.ui.home.HomeActivity;
import com.fealrias.purchasehistory.ui.home.dashboard.balance.MonthlyBalanceFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.list.PurchaseListDashboardFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.fealrias.purchasehistory.util.AndroidUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FullscreenGraphActivity extends AppCompatActivity {
    private static final String TAG = FullscreenGraphActivity.class.getSimpleName();
    private ActivityFullscreenGraphBinding binding;
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    private DashboardComponent dashboardComponent;
    @Inject
    protected PurchaseFilterSingleton filterViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });
        dashboardComponent = getIntent().getParcelableExtra(Constants.Arguments.ARG_COMPONENT);
        if (dashboardComponent != null) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setIcon(R.drawable.arrow_turn_left);
                actionBar.setTitle(dashboardComponent.getTitle());

            }
            RefreshablePurchaseFragment fragment = DashboardComponentsFactory.createFragment(dashboardComponent.getFragmentName());

            if (dashboardComponent.isLandscapeOnly()) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (fragment.getArguments() != null)
                    fragment.getArguments().putInt(Constants.Arguments.EXTERNAL_LEGEND, R.id.legendList);
            }

            binding.verticalFilterBar.filterBtn.setOnClickListener(v -> openFilter());
            binding.filterBar.filterBtn.setOnClickListener(v -> openFilter());

            applyFilter(filterDialog.getFilter());
            filterViewModel.getFilter().observe(this, this::applyFilter);

            if (fragment.getArguments() != null) {
                fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .replace(binding.fullscreenFragmentContainer.getId(), fragment);
            boolean isPieChart = dashboardComponent.getFragmentName().equals("PieChartFragment");
            boolean isMonthlyBalanceFragment = dashboardComponent.getFragmentName().equals("MonthlyBalanceFragment");
            if ((isPieChart || isMonthlyBalanceFragment) && binding.secondaryFragmentContainer != null) {
                binding.secondaryFragmentContainer.setVisibility(View.VISIBLE);
                PurchaseListDashboardFragment listFragment = new PurchaseListDashboardFragment();
                Bundle arguments = listFragment.getArguments();
                if (arguments != null) {
                    arguments.putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
                    if (isMonthlyBalanceFragment) {
                        arguments.putParcelable(Constants.Arguments.ARG_FILTER, ((MonthlyBalanceFragment) fragment).getLocalFilter());
                    }
                }
                transaction.replace(binding.secondaryFragmentContainer.getId(), listFragment);
            } else {
                binding.secondaryFragmentContainer.setVisibility(View.GONE);
            }
            transaction.commit();
        }
    }

    private void openFilter() {
        filterDialog.show(getSupportFragmentManager(), "chart_filter");
    }

    private void applyFilter(PurchaseFilter newFilter) {
        if (binding == null || newFilter == null) return;

        int color = newFilter.getCategoryId() == null ? getResources().getColor(R.color.surfaceA20, getTheme()) : AndroidUtils.getColor(newFilter.getCategoryColor());
        AndroidUtils.tint(binding.filterBar.filterCategoryBtn, color);
        binding.filterBar.filterCategoryBtn.setTextColor(AndroidUtils.getTextColor(color));
        binding.filterBar.filterCategoryBtn.setText(newFilter.getCategoryName() == null ? getString(R.string.category) : newFilter.getCategoryName());
        binding.filterBar.filterDateBtn.setText(newFilter.getDateString());
        AndroidUtils.tint(binding.verticalFilterBar.filterCategoryBtn, color);
        binding.verticalFilterBar.filterCategoryBtn.setTextColor(AndroidUtils.getTextColor(color));
        binding.verticalFilterBar.filterCategoryBtn.setText(newFilter.getCategoryName() == null ? getString(R.string.category) : newFilter.getCategoryName());
        binding.verticalFilterBar.filterDateBtn.setText(newFilter.getDateString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fullscreen_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backPressed();
            return true;
        } else if (item.getItemId() == R.id.action_icon) {
            // Show information about the activity
            new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                    .setTitle(getString(dashboardComponent.getTitle()))
                    .setMessage(getComponentInfo(dashboardComponent))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getComponentInfo(DashboardComponent dashboardComponent) {
        int info = switch (dashboardComponent.getFragmentName()) {
            case "PieChartFragment" -> R.string.help_info_pie_chart;
            case "LineChartFragment" -> R.string.help_info_line_chart;
            case "AccumulativeChartFragment" -> R.string.help_info_accumulative_line_chart;
            case "MonthlyBalanceFragment" -> R.string.help_info_monthly_balance;
            case "BarChartFragment" -> R.string.help_info_stacked_bar_chart;
            case "PurchaseListPurchaseFragment" -> R.string.help_info_purchases_list;
            default ->
                    throw new IllegalStateException("Unexpected value: " + dashboardComponent.getFragmentName());
        };
        return getString(info);
    }

    public void backPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i(TAG, "popping backstack");
            fm.popBackStack();
        } else {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}