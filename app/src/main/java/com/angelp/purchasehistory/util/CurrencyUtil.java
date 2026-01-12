package com.angelp.purchasehistory.util;

import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import java.util.Locale;

public class CurrencyUtil {
    public static String formatPrice(ScheduledExpenseView scheduledExpense) {
        return String.format(Locale.US, "%.2f", scheduledExpense.getPrice()) + getCurrencyString(scheduledExpense.getCurrency());
    }

    public static String getCurrencyString(String currency) {
        return switch (currency) {
            case "BGN" -> "лв";
            case "EUR" -> "€";
            default -> "";
        };
    }

}
