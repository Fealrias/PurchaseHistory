package com.fealrias.purchasehistory.data.model;

import lombok.Data;

@Data
public class LegendItem {
    private String label;
    private int color;

    public LegendItem(String label, int color) {
        this.label = label;
        this.color = color;
    }
}
