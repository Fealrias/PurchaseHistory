package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.DialogGraphDetailsBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.PurchaseListView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PurchasesPerDayDialog extends DialogFragment {

    public static final String TOTAL_SUM = "total_sum";
    public static final String TITLE = "title";
    private final String TAG = PurchasesPerDayDialog.class.getSimpleName();
    private final DateTimeFormatter dtf_short = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    private final DateTimeFormatter dtf_long = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    private final int maxSize = 10;
    @Inject
    PurchaseClient purchaseClient;
    private DialogGraphDetailsBinding binding;
    private PurchasesAdapter purchasesAdapter;
    private PurchaseFilter filter;
    private TextView title;

    public PurchasesPerDayDialog() {
        Bundle bundle = new Bundle();
        setArguments(bundle);
    }

    public PurchasesPerDayDialog(CalendarReportEntry calendarReportEntry) {
        Bundle bundle = new Bundle();
        PurchaseFilter purchaseFilter = new PurchaseFilter(calendarReportEntry.getLocalDate());
        bundle.putParcelable(Constants.Arguments.ARG_FILTER, purchaseFilter);
        bundle.putString(TITLE, calendarReportEntry.getLocalDate().format(dtf_long));
        bundle.putFloat(TOTAL_SUM, calendarReportEntry.getSum().floatValue());
        setArguments(bundle);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Set all the title, button etc. for the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        LayoutInflater layoutInflater = getLayoutInflater();
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null, false);
        binding = DialogGraphDetailsBinding.inflate(layoutInflater, null, false);
        builder.setCustomTitle(titleView);
        title = titleView.findViewById(R.id.dialogTitle);
        title.setText(getArguments().getString(TITLE));
        binding.message.setText(AndroidUtils.formatCurrency(getArguments().getFloat(TOTAL_SUM)));
        builder.setView(binding.getRoot());
        filter = getArguments().getParcelable(Constants.Arguments.ARG_FILTER);
        initializePurchasesRecyclerView(maxSize, filter);
        updateFilterButtons(filter.getFrom());
        binding.nextDayButton.setOnClickListener((v) -> updateFilter(filter.getFrom().plusDays(1)));
        binding.previousDayButton.setOnClickListener((v) -> updateFilter(filter.getFrom().minusDays(1)));
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private void updateFilter(LocalDate localDate) {
        filter.setFrom(localDate);
        filter.setTo(localDate);
        updateFilterButtons(localDate);
        refresh(filter);
    }

    private void updateFilterButtons(LocalDate localDate) {
        binding.nextDayButton.setText(localDate.plusDays(1).format(dtf_short));
        binding.previousDayButton.setText(localDate.minusDays(1).format(dtf_short));
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
            String sum = AndroidUtils.formatCurrency(purchaseListView.getTotalSum());
            setupShowMoreButton(purchases.size(), maxSize);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding != null) {
                    binding.message.setText(getString(R.string.total_sum, sum));
                    binding.purchaseList.setLayoutManager(llm);
                    binding.purchaseList.setItemAnimator(new DefaultItemAnimator());
                    binding.purchaseList.setAdapter(purchasesAdapter);
                }
            });
        }).start();
    }

    private void setupShowMoreButton(int purchaseSize, int maxSize) {
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
            binding.seeAllButton.setText(getString(R.string.see_all_n_purchases, purchaseSize));
            binding.seeAllButton.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
            binding.seeAllBackdrop.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
        });
    }

    public void refresh(PurchaseFilter filter) {
        if (purchasesAdapter == null || binding == null) {
            Log.w(TAG, "refresh: Purchases adapter is missing. Skipping refresh");
            return;
        }
        new Thread(() -> {
            PurchaseListView purchaseListView = purchaseClient.getAllPurchases(filter);
            purchasesAdapter.getPurchaseViews().clear();
            Log.i(TAG, "Received purchases list with size of " + purchaseListView.getContent().size());
            updateUI(purchaseListView);
        }).start();
    }

    private void updateUI(PurchaseListView allPurchases) {
        new Handler(Looper.getMainLooper()).post(() -> {
            title.setText(filter.getFrom().format(dtf_long));
            binding.message.setText(AndroidUtils.formatCurrency(allPurchases.getTotalSum()));

            purchasesAdapter.setPurchaseViews(allPurchases.getContent());
            if (binding == null) return;
            updateSeeAllButton(allPurchases.getContent().size(), maxSize);
        });
    }
}
