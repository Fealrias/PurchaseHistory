package com.fealrias.purchasehistory.ui.home.dashboard.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.AppColorCollection;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.databinding.FragmentLineChartBinding;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.fealrias.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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
public class LineChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM yy");
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    private FragmentLineChartBinding binding;
    private AppColorCollection appColorCollection;
    private Typeface tf;
    private PurchasesPerDayDialog dialog;
    private Integer legendId;

    public LineChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            legendId = getArguments().getInt(Constants.Arguments.EXTERNAL_LEGEND);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentLineChartBinding.inflate(inflater, container, false);
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
        setData(filterViewModel.getFilterValue());
    }

    private void initGraph(LineChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd", tf);
        chart.setOnChartValueSelectedListener(this);
        chart.setNoDataText(getString(R.string.no_data));

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
                lineDataSet.setMode(LineDataSet.Mode.LINEAR);
                lineDataSet.setDrawFilled(true);
                Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fade_red);

                if (i < colors.size()) {
                    int color = colors.get(i++);
                    lineDataSet.setColor(color);
                    lineDataSet.setCircleColor(color);
                    if (drawable != null) drawable.setTint(color);

                }
                lineDataSet.setFillDrawable(drawable);
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
        if(calendarReport!=null){
            for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
                LocalDate key = calendarReportEntry.getLocalDate().withDayOfMonth(1);
                List<Entry> entries = map.computeIfAbsent(key, (k) -> new ArrayList<>());
                Entry entry = parseEntries(calendarReportEntry);
                entries.add(entry);
                map.put(key, entries);
            }
        }
        return map;
    }

    private void notifyDataChanged(LineData data) {
        if (data == null) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.lineChartView.setData(data);
            binding.lineChartView.getData().notifyDataChanged();
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

    private Entry parseEntries(CalendarReportEntry entry) {
        float x = ((Long) entry.getLocalDate()
                .withYear(LocalDate.now().getYear())
                .with(Month.JANUARY)
                .toEpochDay()).floatValue();

        return new Entry(x, entry.getSum().floatValue(), entry);
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
}