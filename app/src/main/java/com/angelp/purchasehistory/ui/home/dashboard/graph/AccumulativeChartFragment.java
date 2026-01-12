package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.angelp.purchasehistory.databinding.FragmentAccumulativeChartBinding;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.SettingsClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccumulativeChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM yy");
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    @Inject
    SettingsClient settingsClient;
    private FragmentAccumulativeChartBinding binding;
    private boolean showLimit;
    private AppColorCollection appColorCollection;
    private Typeface tf;
    private PurchasesPerDayDialog dialog;
    private Integer legendId;

    public AccumulativeChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showLimit = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
            legendId = getArguments().getInt(Constants.Arguments.EXTERNAL_LEGEND);

        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentAccumulativeChartBinding.inflate(inflater, container, false);

        appColorCollection = new AppColorCollection(inflater.getContext());
        tf = ResourcesCompat.getFont(inflater.getContext(), R.font.inter);
        super.setLoadingScreen(binding.loadingBar);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        initGraph(binding.lineChartView);
        if (!showLimit) {
            binding.editLimitButton.setVisibility(View.GONE);
        }
        binding.editLimitButton.setOnClickListener((v) -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.putExtra(Constants.Arguments.ACTIVITY_NAVIGATE_TO, Constants.SettingsLocations.EDIT_MONTHLY_LIMIT);
            startActivity(intent);
        });
        setData(filterViewModel.getFilterValue());
    }

    private void initGraph(LineChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd", tf);
        chart.setOnChartValueSelectedListener(this);
        chart.setNoDataText(getString(R.string.no_data));
        setupMonthlyLimits(chart);
    }


    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            isRefreshing.postValue(true);
            CalendarReport calendarReport = purchaseClient.getCalendarReport(filter);
            Map<LocalDate, List<Entry>> entriesMap = getEntries(calendarReport);
            ArrayList<Integer> colors = getColors();
            LineData data = new LineData();
            int i = 0;
            for (Map.Entry<LocalDate, List<Entry>> entry : entriesMap.entrySet()) {
                List<Entry> entries = entry.getValue();
                LineDataSet lineDataSet = new LineDataSet(entries, "Purchases");
                lineDataSet.setDrawCircleHole(false);
                lineDataSet.setValueTypeface(tf);
                lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                if (i < colors.size()) {
                    int color = colors.get(i++);
                    lineDataSet.setColor(color);
                    lineDataSet.setFillColor(color);
                    lineDataSet.setCircleColor(color);
                }
                lineDataSet.setLabel(entry.getKey().format(DATE_TIME_FORMATTER));
                data.addDataSet(lineDataSet);
            }
            data.setValueTextColor(appColorCollection.getForegroundColor());
            data.setValueFormatter(new CurrencyValueFormatter(AndroidUtils.getPreferredCurrencySymbol()));
            notifyDataChanged(data);
            isRefreshing.postValue(false);
        }).start();
    }

    private ArrayList<Integer> getColors() {
        ArrayList<Integer> list = new ArrayList<>();
        Context context = getContext();
        Resources resources = getResources();
        if (context == null) return list;
        list.add(resources.getColor(R.color.line_chart_color_1, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_2, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_3, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_4, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_5, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_6, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_7, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_8, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_9, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_10, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_11, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_12, context.getTheme()));
        return list;
    }

    @NotNull
    private Map<LocalDate, List<Entry>> getEntries(CalendarReport calendarReport) {
        Map<LocalDate, List<Entry>> map = new HashMap<>();
        for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
            LocalDate key = calendarReportEntry.getLocalDate().withDayOfMonth(1);
            List<Entry> entries = map.computeIfAbsent(key, (k) -> new ArrayList<>());
            float sum = !entries.isEmpty() ? entries.get(entries.size() - 1).getY() : 0f;
            Entry entry = parseEntries(calendarReportEntry, sum);
            entries.add(entry);
            map.put(key, entries);
        }
        return map;
    }

    private void notifyDataChanged(LineData data) {
        if (data == null) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.lineChartView.setData(data);
            binding.lineChartView.notifyDataSetChanged();
            binding.lineChartView.animateY(1000);
            binding.lineChartView.invalidate();
            new Thread(()->{
                if (legendId != null && getActivity()!=null) {
                    Legend legend = binding.lineChartView.getLegend();
                    ListView listView = getActivity().findViewById(legendId);
                    legend.setEnabled(AndroidUtils.setLegendList(legend, listView));
                }
            }).start();
        });

    }

    private Entry parseEntries(CalendarReportEntry entry, float sum) {
        float x = ((Long) entry.getLocalDate()
                .withYear(LocalDate.now().getYear())
                .with(Month.JANUARY)
                .toEpochDay()).floatValue();

        return new Entry(x, entry.getSum().floatValue() + sum, entry);
    }

    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        new Thread(() -> setData(filter)).start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        if (dialog != null && dialog.isAdded()) dialog.dismiss();

        CalendarReportEntry data = (CalendarReportEntry) e.getData();
        if (!data.getSum().equals(BigDecimal.ZERO)) {
            dialog = new PurchasesPerDayDialog(data);
            dialog.show(getParentFragmentManager().beginTransaction(), "DialogFragment");
        }

    }

    @Override
    public void onNothingSelected() {

    }

    private void setupMonthlyLimits(LineChart chart) {
        new Thread(() -> {
            List<MonthlyLimitView> monthlyLimitList = settingsClient.getMonthlyLimits();
            if (monthlyLimitList != null && !monthlyLimitList.isEmpty()) {
                for (MonthlyLimitView monthlyLimit : monthlyLimitList) {
                    LimitLine l = new LimitLine(monthlyLimit.getValue().floatValue());
                    l.setLineWidth(2);
                    l.setLabel(monthlyLimit.getLabel());
                    l.setTextColor(appColorCollection.getForegroundColor());
                    l.setTypeface(tf);
                    chart.getAxisLeft().addLimitLine(l);
                }
            }
        }).start();
    }
}