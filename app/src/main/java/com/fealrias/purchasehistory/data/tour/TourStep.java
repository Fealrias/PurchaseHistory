package com.fealrias.purchasehistory.data.tour;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourStep {
    private int id;
    private int primaryText;
    private int secondaryText;
}