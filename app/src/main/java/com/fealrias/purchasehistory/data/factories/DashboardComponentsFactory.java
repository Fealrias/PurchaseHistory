package com.fealrias.purchasehistory.data.factories;

import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.balance.MonthlyBalanceFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.graph.AccumulativeChartFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.graph.BarChartFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.graph.LineChartFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.list.PurchaseListDashboardFragment;
import com.fealrias.purchasehistory.ui.home.dashboard.pie.PieChartFragment;

public final class DashboardComponentsFactory {
    public static RefreshablePurchaseFragment createFragment(String type) {
        return switch (type) {
            case "PieChartFragment" -> new PieChartFragment();
            case "PurchaseListPurchaseFragment" -> new PurchaseListDashboardFragment();
            case "LineChartFragment" -> new LineChartFragment();
            case "AccumulativeChartFragment" -> new AccumulativeChartFragment();
            case "BarChartFragment" -> new BarChartFragment();
            case "MonthlyBalanceFragment" -> new MonthlyBalanceFragment();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
