package com.fealrias.purchasehistory.ui.home.dashboard.graph;

import com.github.mikephil.charting.formatter.ValueFormatter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class DayAxisValueFormatter extends ValueFormatter {

    private String format = "dd MMM";

    public DayAxisValueFormatter(String format) {
        this.format = format;
    }

    @Override
    public String getFormattedValue(float value) {
        LocalDate localDate = LocalDate.ofEpochDay(((Float) value).longValue());
        return localDate.format(DateTimeFormatter.ofPattern(format));
    }
}
