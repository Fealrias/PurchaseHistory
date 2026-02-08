package com.fealrias.purchasehistory.data.filters;

import android.widget.Button;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TimeButton {
    private Button button;
    private LocalDate from;

    public TimeButton(Button button, LocalDate localDate) {
        this.button = button;
        this.from = localDate;
    }
}
