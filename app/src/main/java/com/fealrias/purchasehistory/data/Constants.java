package com.fealrias.purchasehistory.data;

import android.content.Context;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.data.tour.TourStep;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Constants {
    public static final int GRAPH_MIN_HEIGHT = 1000;
    public final static List<DashboardComponent> DEFAULT_COMPONENTS = new ArrayList<>();
    public static final List<TourStep> tourSteps = new ArrayList<>();
    public static final HashMap<String, Integer> errorsMap = new HashMap<>();
    public interface CURRENCY {
        String BGN= "BGN";
        String EUR = "EUR";
    }

    public static final List<String> CURRENCY_LIST = List.of(CURRENCY.EUR,CURRENCY.BGN);

    static {
        DEFAULT_COMPONENTS.add(new DashboardComponent("MonthlyBalanceFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("PieChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("LineChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("BarChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("AccumulativeChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("PurchaseListPurchaseFragment"));
    }

    static {
        tourSteps.add(new TourStep(R.id.navigation_dashboard, R.string.tour_navigation_dashboard, R.string.tour_navigation_dashboard_secondary));
        tourSteps.add(new TourStep(R.id.filter_btn, R.string.tour_filter_button, R.string.tour_filter_button_secondary));
        tourSteps.add(new TourStep(R.id.navigation_qrscanner, R.string.tour_navigation_qrscanner, R.string.tour_navigation_qrscanner_secondary));
        tourSteps.add(new TourStep(R.id.qr_categoryAddButton, R.string.tour_navigation_category, R.string.tour_navigation_category_secondary));
        tourSteps.add(new TourStep(R.id.navigation_scheduled_expenses, R.string.tour_navigation_scheduled_expenses, R.string.tour_navigation_scheduled_expenses_secondary));
        tourSteps.add(new TourStep(R.id.navigation_profile, R.string.tour_navigation_profile, R.string.tour_navigation_profile_secondary));
    }

    static {
        errorsMap.put("1001", R.string.err1001);
        errorsMap.put("1002", R.string.err1002);
        errorsMap.put("1003", R.string.err1003);
        errorsMap.put("1004", R.string.err1004);
        errorsMap.put("1005", R.string.err1005);
        errorsMap.put("1006", R.string.err1006);
        errorsMap.put("1007", R.string.err1007);
        errorsMap.put("1008", R.string.err1008);
        errorsMap.put("1009", R.string.err1009);
        errorsMap.put("1010", R.string.err1010);
        errorsMap.put("1011", R.string.err1011);
        errorsMap.put("1012", R.string.err1012);
        errorsMap.put("1013", R.string.err1013);
        errorsMap.put("1014", R.string.err1014);
    }

    public static PurchaseFilter getDefaultFilter() {
        return new PurchaseFilter(LocalDate.now().withDayOfMonth(1), LocalDate.now(), null, null, null, null);
    }

    public static PurchaseFilter getFilter30Days() {
        return new PurchaseFilter(LocalDate.now().minusDays(30), LocalDate.now(), null, null, null, null);
    }

    public interface Preferences {
        String IS_FIRST_TIME_OPEN = "isFirstTimeOpen_HomeActivity";
        String DASHBOARD_PREFS = "dashboard_prefs";
        String APP_PREFERENCES = "app_preferences";
        String SILENCED_NOTIFICATIONS = "silenced_notifications";
        String PREFERRED_LANGUAGE = "language";
    }

    public interface Arguments {
        String PURCHASE_EDIT_DIALOG_ID_KEY = "purchaseId";
        String PURCHASE_EDIT_DIALOG_CONTENT_KEY = "purchaseContent";
        String ARG_SHOW_FILTER = "show_filter";
        String NOTIFICATION_EXTRA_ARG = "scheduledNotifications";
        String ARG_MAX_SIZE = "max_size";
        String ARG_FILTER = "purchases_filter";
        String OPEN_CAMERA = "open_camera";
        String EXTERNAL_LEGEND = "external_legend";
        String MARGIN_BOTTOM = "marginBottom";
        String VIEW_ID = "viewId";
        String ARG_COMPONENT = "component";
        String DASHBOARD_FILTER = "dashboard_filter";
        String ACTIVITY_NAVIGATE_TO = "fragment_name";
    }
    public interface SettingsLocations {
        String EDIT_CATEGORY = "edit_category";
        String EDIT_MONTHLY_LIMIT = "edit_monthly_limit";
    }
    @NotNull
    public static CategoryView getDefaultCategory(Context context) {
        return new CategoryView(null, context.getString(R.string.choose_category), "#ffffff");
    }
}
