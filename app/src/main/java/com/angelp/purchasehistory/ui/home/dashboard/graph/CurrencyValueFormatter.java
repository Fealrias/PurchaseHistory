package com.angelp.purchasehistory.ui.home.dashboard.graph;

import com.angelp.purchasehistory.util.AndroidUtils;

public class CurrencyValueFormatter extends com.github.mikephil.charting.formatter.ValueFormatter {

    private final String string;

    public CurrencyValueFormatter(String string) {
        this.string = string;
    }

    @Override
    public String getFormattedValue(float value) {
        if (value <= 0) {
            return "";
        } else {
            return AndroidUtils.formatCurrency(value) + string;
        }
    }
}
