package com.angelp.purchasehistory.ui.home.dashboard.balance;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.BalanceCardBinding;
import com.angelp.purchasehistory.ui.home.settings.AddMonthlyLimitDialog;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.SettingsClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.MonthlyBalance;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;

@AndroidEntryPoint
public class MonthlyBalanceFragment extends RefreshablePurchaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    SettingsClient settingsClient;
    @Inject
    PurchaseClient purchaseClient;
    private AddMonthlyLimitDialog addMonthlyLimitDialog;
    private BalanceCardBinding binding;
    @Getter
    private final PurchaseFilter localFilter = new PurchaseFilter();

    public MonthlyBalanceFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        isRefreshing.observe(requireActivity(), (isRefreshing) -> {
            if (binding != null)
                binding.balanceBar.setIndeterminate(isRefreshing);
        });
        addMonthlyLimitDialog = new AddMonthlyLimitDialog((limit) -> refresh(filterViewModel.getFilterValue()));
        binding.editLimit.setOnClickListener((v) -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.putExtra(Constants.Arguments.ACTIVITY_NAVIGATE_TO, Constants.SettingsLocations.EDIT_MONTHLY_LIMIT);
            startActivity(intent);
        });
        initialize(filterViewModel.getFilterValue());
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BalanceCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initialize(PurchaseFilter mainFilter) {
        localFilter.setFrom(mainFilter.getFrom().withDayOfMonth(1));
        localFilter.setTo(mainFilter.getFrom().withDayOfMonth(mainFilter.getFrom().getMonth().length(mainFilter.getFrom().isLeapYear())));

        new Thread(() -> {
            isRefreshing.postValue(true);
            MonthlyBalance balance = purchaseClient.getMonthlyBalance(localFilter);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding == null) return;
                String string = getString(R.string.showing_balance_for_s, localFilter.getFrom().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                binding.subHeading.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY));
                binding.setupLimitBtn.setOnClickListener((v) -> addMonthlyLimitDialog.show(getParentFragmentManager(), "dashboard_Add_monthly_limit"));
                binding.spendingText.setText(AndroidUtils.formatCurrency(balance.sum()));
                if (localFilter.getFrom().withDayOfMonth(1).equals(LocalDate.now().withDayOfMonth(1)))
                    binding.subHeading.setVisibility(View.GONE);
                else binding.subHeading.setVisibility(View.VISIBLE);
            });
            isRefreshing.postValue(false);
            if (balance.monthlyLimit() == null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null) return;
                    binding.dataView.setVisibility(View.GONE);
                    binding.missingView.setVisibility(View.VISIBLE);
                });
                return;
            }

            updateUI(balance);
        }).start();
    }

    private void updateUI(MonthlyBalance balance) {
        binding.balanceBar.post(() -> {
            int progress = balance.progress().intValue();
            if (binding != null) {
                binding.dataView.setVisibility(View.VISIBLE);
                binding.missingView.setVisibility(View.GONE);
                int color = requireContext().getColor(R.color.text);
                Drawable drawable = binding.balanceBar.getProgressDrawable().mutate();

                if (balance.overspent()) {
                    binding.balanceBar.setScaleX(-1);
                    drawable.setColorFilter(new BlendModeColorFilter(requireContext().getColor(R.color.dangerA10), BlendMode.SRC_IN));
                    progress = balance.max().subtract(balance.monthlyLimit().getValue()).intValue();
                    color = requireContext().getColor(R.color.dangerA10);
                    binding.heading.setText(R.string.overspent);
                } else {
                    binding.balanceBar.setScaleX(1);
                    drawable.setColorFilter(new BlendModeColorFilter(requireContext().getColor(R.color.successA10), BlendMode.SRC_IN));
                    binding.heading.setText(R.string.remaining);
                    binding.remainingBalanceText.setTextColor(requireContext().getColor(R.color.text));
                }
                binding.balanceBar.setProgressDrawable(drawable);
                binding.balanceBar.invalidateDrawable(drawable);

                binding.balanceBar.setMin(0);
                binding.balanceBar.setMax(balance.max().intValue());
                binding.balanceBar.setProgress(progress);

                binding.balanceBar.invalidate();

                binding.remainingBalanceText.setText(AndroidUtils.formatCurrency(balance.remaining().floatValue()));
                String text = AndroidUtils.formatCurrency(balance.monthlyLimit().getValue());
                String label = balance.monthlyLimit().getLabel();
                if (!label.isBlank()) text = text + "(" + label + ")";
                binding.limitText.setText(text);
                binding.heading.setTextColor(color);
                binding.remainingBalanceText.setTextColor(color);
            }

        });
    }

    public void refresh(PurchaseFilter mainFilter) {
        if (binding == null) return;
        initialize(mainFilter);
    }
}