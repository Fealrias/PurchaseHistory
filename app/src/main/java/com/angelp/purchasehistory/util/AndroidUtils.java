package com.angelp.purchasehistory.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.os.LocaleListCompat;

import com.angelp.purchasehistory.MainActivity;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.receivers.scheduled.NotificationHelper;
import com.angelp.purchasehistory.ui.home.dashboard.graph.DayAxisValueFormatter;
import com.angelp.purchasehistory.ui.home.qr.CategorySpinnerAdapter;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.enums.ScheduledPeriod;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class AndroidUtils {
    public static final List<String> SCHEDULED_PERIOD_LIST = Arrays.stream(ScheduledPeriod.values()).map(Enum::toString).collect(Collectors.toList());

    public static void shareString(String token, String title, Context context) {
        Log.i("Sharing", "Attempting to share a string.");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, token);
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);

        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void logout(Context context, @Nullable PurchaseClient purchaseClient) {
        PurchaseHistoryApplication.getInstance().userToken.postValue(null);
        PurchaseHistoryApplication.getInstance().loggedUser.postValue(null);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ShortcutManagerCompat.removeAllDynamicShortcuts(context);
        NotificationHelper.cancelAllScheduledNotifications(context);
        if (purchaseClient != null) purchaseClient.cleanCache();
        SharedPreferences player = context.getSharedPreferences("player", MODE_PRIVATE);
        player.edit().clear().apply();
        context.startActivity(intent);
    }

    @ColorInt
    public static int getColor(CategoryView category) {
        if (category == null || category.getColor() == null || category.getColor().isBlank())
            return Color.GRAY;
        String colorHex = category.getColor();
        return getColor(colorHex);
    }

    @ColorInt
    public static int getColor(String colorHex) {
        try {
            return Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            Log.e("AndroidUtils", "Invalid color: " + colorHex);
            return Color.GRAY;
        }
    }

    public static int getTextColor(int bgColor) {
        if (Color.luminance(bgColor) > 0.5)
            return Color.BLACK;
        else
            return Color.WHITE;
    }

    // A placeholder username validation check
    public static boolean isUserNameInvalid(String username) {
        if (username == null || username.trim().length() <= 5) {
            return true;
        } else {
            return username.trim().isEmpty();
        }
    }

    // A placeholder email validation check
    public static boolean isEmailInvalid(String email) {
        if (email == null) {
            return true;
        } else return !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    // A placeholder password validation check
    public static boolean isPasswordInvalid(String password) {
        return password == null || password.trim().length() <= 5;
    }

    public static void openCsvFile(Activity context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("OPEN_CSV", "No app found to open CSV file", e);
        }
    }

    @NotNull
    public static String getNextTimestampString(ScheduledExpenseView scheduledExpense) {
        if (scheduledExpense == null || scheduledExpense.getPeriod() == null) {
            return "";
        }

        LocalDateTime nextTimestamp = scheduledExpense.getPeriod().getNextTimestamp(scheduledExpense.getTimestamp());

        return (String) DateUtils.getRelativeTimeSpanString(nextTimestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static void setNextTimestampString(TextView textView, ScheduledExpenseView scheduledExpense) {
        if (scheduledExpense == null || scheduledExpense.getPeriod() == null) {
            textView.setText(R.string.no_period_selected);
            return;
        }
        if (!scheduledExpense.isEnabled()) {
            textView.setText(R.string.disabled);
            return;
        }
        String nextTimestampString = getNextTimestampString(scheduledExpense);
        if (nextTimestampString.isBlank()) {
            textView.setText("");
            return;
        }
        textView.setText(getNextTimestampString(scheduledExpense));
    }

    @NotNull
    public static String formatCurrency(float price) {
        return formatCurrency(price, AndroidUtils.getPreferredCurrency());
    }
    @NotNull
    public static String format(BigDecimal price) {
        float f = price == null ? 0f : price.floatValue();
        return String.format(Locale.US, "%.2f", f);
    }
    @NotNull
    public static String formatCurrency(BigDecimal price) {
        return formatCurrency(price, AndroidUtils.getPreferredCurrency());
    }

    @NotNull
    public static String formatCurrency(BigDecimal price, String currency) {
        float f = price == null ? 0f : price.floatValue();
        return formatCurrency(f, currency);
    }

    @NotNull
    public static String formatCurrency(float price, String currency) {
        return String.format(Locale.US, "%.2f %s", price, CurrencyUtil.getCurrencyString(currency));
    }

    public static String getPreferredCurrency() {
        UserView value = PurchaseHistoryApplication.getInstance().loggedUser.getValue();
        return value == null ? "" : value.getPreferredCurrency();
    }
    public static String getPreferredCurrencySymbol() {
        return CurrencyUtil.getCurrencyString(getPreferredCurrency());
    }

    public static void initChart(BarLineChartBase<?> chart, AppColorCollection colors, String format, Typeface tf) {

        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setHighlightPerTapEnabled(true);
        chart.setHighlightPerDragEnabled(false);
        chart.getDescription().setEnabled(false);
//        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setMinimumHeight(Constants.GRAPH_MIN_HEIGHT);


        if (chart instanceof BarChart barChart) {
            barChart.setDrawBarShadow(false);
            barChart.setDrawValueAboveBar(true);
        }

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(colors.getForegroundColor());
        leftAxis.setTypeface(tf);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f); // fallback

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        DayAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(format);
        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xAxisFormatter);
        xLabels.setGranularity(1f);
        xLabels.setTextColor(colors.getForegroundColor());
        xLabels.setTypeface(tf);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setTypeface(tf);
        l.setTextColor(colors.getForegroundColor());
        l.setDrawInside(false);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);

    }

    public static void showSuccessAnimation(View view) {
        if (view == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.BaseDialogStyle);
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View customView = inflater.inflate(R.layout.success_toast, null);
        builder.setView(customView);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null)
                dialog.getWindow().getAttributes().windowAnimations = R.anim.success_toast;
            dialog.show();
            handler.postDelayed(dialog::dismiss, 1000);
        });

    }

    public static boolean validateCategoryValues(AutoCompleteTextView nameView, AutoCompleteTextView colorView) {
        Context context = nameView.getContext();
        String name = nameView.getText().toString();
        String color = colorView.getText().toString();
        if (name.trim().isEmpty()) {
            nameView.setError(context.getString(R.string.error_name_empty));
            return false;
        } else nameView.setError(null);
        if (color.trim().isEmpty()) {
            colorView.setError(context.getString(R.string.error_color_empty));
            return false;
        } else if (!color.startsWith("#")) {
            colorView.setError(context.getString(R.string.error_color_invalid));
            return false;
        } else if (color.length() < 6) {
            colorView.setError(context.getString(R.string.error_color_length));
            return false;
        } else colorView.setError(null);
        return true;
    }

    public static void validateNumber(String string) {
        try {
            BigDecimal bigDecimal = new BigDecimal(string);
            if (bigDecimal.floatValue() <= 0) {
                throw new IllegalArgumentException("Invalid number: " + string);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + string);
        }
    }

    /**
     * Sets the provided legend onto the ListView.
     *
     * @return true if it failed and normal Legend should be used
     */
    public static boolean setLegendList(Legend legend, ListView listView) {
        try {
            if (listView == null) return true;
            List<CategoryView> legendItems = new ArrayList<>();
            LegendEntry[] entries = legend.getEntries();
            for (int j = 0; j < entries.length; j++) {
                LegendEntry item = entries[j];
                String color = String.format("#%06X", (0xFFFFFF & item.formColor));
                CategoryView categoryView = new CategoryView((long) j, item.label, color);
                new Handler(Looper.getMainLooper()).post(() -> legendItems.add(categoryView));
            }
            CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(listView.getContext(), CategorySpinnerAdapter.SIZE_SMALL, legendItems);
            new Handler(Looper.getMainLooper()).post(() -> listView.setAdapter(adapter));

            return false;
        } catch (NullPointerException e) {
            Log.e("Legend", "Failed to fetch legend list");
            return true;
        }
    }

    public static boolean isFirstTimeOpen(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.APP_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, true);
    }

    public static void setLocale(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    public static void tint(View view, int color) {
        Drawable mutate1 = view.getBackground().mutate();
        mutate1.setTint(color);
        view.setBackground(mutate1);
    }
}