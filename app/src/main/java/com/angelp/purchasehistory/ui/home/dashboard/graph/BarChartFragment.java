package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentBarChartBinding;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BarChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    private FragmentBarChartBinding binding;
    private PurchasesPerDayDialog dialog;
    private AppColorCollection appColorCollection;
    private Typeface tf;
    private Integer legendId;

    public BarChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    private static Map<LocalDate, List<CalendarReportEntry>> prepareContent(PurchaseFilter filter, CalendarReport calendarReport, List<CategoryView> categories) {
        Map<LocalDate, List<CalendarReportEntry>> content = new HashMap<>();
        LocalDate dateIterator = filter.getFrom();
        for (; dateIterator.isBefore(filter.getTo().plusDays(1)); dateIterator = dateIterator.plusDays(1L)) {
            List<CalendarReportEntry> list = content.computeIfAbsent(dateIterator, (key) -> new ArrayList<>());

            for (CategoryView category : categories) {
                boolean added = false;
                for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
                    if (calendarReportEntry.getLocalDate().equals(dateIterator) && calendarReportEntry.getCategory().getId().equals(category.getId())) {
                        list.add(calendarReportEntry);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    list.add(new CalendarReportEntry(dateIterator.format(DateTimeFormatter.ISO_LOCAL_DATE), BigDecimal.ZERO, 0L, new CategoryView()));
                }
            }
            content.put(dateIterator, list);
        }
        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            legendId = getArguments().getInt(Constants.Arguments.EXTERNAL_LEGEND);

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentBarChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        tf = ResourcesCompat.getFont(inflater.getContext(), R.font.inter);
        super.setLoadingScreen(binding.loadingBar);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        initGraph(binding.barChartView);
        setData(filterViewModel.getFilterValue());
    }

    private void initGraph(BarChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd MMM", tf);
        chart.setOnChartValueSelectedListener(this);
        chart.setNoDataText(getString(R.string.no_data));
    }

    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            isRefreshing.postValue(true);
            CalendarReport calendarReport = purchaseClient.getCategorizedCalendarReport(filter);
            List<CategoryView> allCategories = purchaseClient.getAllCategories();
            updateChart(filter, calendarReport, allCategories);
            isRefreshing.postValue(false);
        }).start();
    }

    private void updateChart(PurchaseFilter filter, CalendarReport calendarReport, List<CategoryView> allCategories) {
        Map<LocalDate, List<CalendarReportEntry>> content = prepareContent(filter, calendarReport, allCategories);
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();

        for (CategoryView allCategory : allCategories) {
            labels.add(allCategory.getName());
            int color = AndroidUtils.getColor(allCategory);
            colors.add(color);
        }
        LocalDate dateIterator = filter.getFrom();
        for (; dateIterator.isBefore(filter.getTo().plusDays(1)); dateIterator = dateIterator.plusDays(1L)) {
            entries.add(parseBarEntries(dateIterator, content.get(dateIterator)));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Purchases");
        barDataSet.setValueTypeface(tf);
        barDataSet.setDrawIcons(false);
        if (!colors.isEmpty())
            barDataSet.setColors(colors);
        barDataSet.setStackLabels(labels.toArray(new String[0]));
        BarData data = new BarData(barDataSet);
        data.setValueTextColor(appColorCollection.getForegroundColor());

        barDataSet.setValueTextColor(appColorCollection.getForegroundColor());
        barDataSet.setValueTypeface(tf);
        barDataSet.setValueFormatter(new CurrencyValueFormatter(AndroidUtils.getPreferredCurrencySymbol()));

        data.setBarWidth(0.9f);
        notifyDataChanged(data);
    }

    private void notifyDataChanged(BarData data) {
        if (data == null || data.getDataSetCount() == 0) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.barChartView.setData(data);
            binding.barChartView.notifyDataSetChanged();
            binding.barChartView.animateY(1000);
            new Thread(() -> {
                if (legendId != null && getActivity() != null) {
                    Legend legend = binding.barChartView.getLegend();
                    ListView listView = getActivity().findViewById(legendId);
                    legend.setEnabled(AndroidUtils.setLegendList(legend, listView));
                }
            }).start();
        });
    }

    private BarEntry parseBarEntries(LocalDate date, List<CalendarReportEntry> entries) {
        float[] list = new float[entries.size()];
        float x = ((Long) date.toEpochDay()).floatValue();
        for (int i = 0; i < entries.size(); i++) {
            CalendarReportEntry e = entries.get(i);
            float floatValue = e.getSum().floatValue();
            list[i] = floatValue;
        }
        return new BarEntry(x, list, entries);
    }
    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        new Thread(() -> setData(filter)).start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        List<CalendarReportEntry> data = (List<CalendarReportEntry>) e.getData();
        if (data == null || data.isEmpty())
            return;

        if (dialog != null && dialog.isAdded()) dialog.dismiss();
        BigDecimal sum = BigDecimal.ZERO;
        for (CalendarReportEntry datum : data) {
            sum = sum.add(datum.getSum());
        }
        if (!sum.equals(BigDecimal.ZERO)) {
            dialog = new PurchasesPerDayDialog(data.get(0));
            dialog.show(getParentFragmentManager().beginTransaction(), "DialogFragment");
        }

    }

    @Override
    public void onNothingSelected() {
        if (dialog != null && dialog.isAdded()) dialog.dismiss();
    }
}