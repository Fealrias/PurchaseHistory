package com.angelp.purchasehistory.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardComponent implements Parcelable {
    public static final Parcelable.Creator<DashboardComponent> CREATOR = new Parcelable.Creator<>() {
        @Override
        public DashboardComponent createFromParcel(Parcel in) {
            return new DashboardComponent(in);
        }

        @Override
        public DashboardComponent[] newArray(int size) {
            return new DashboardComponent[size];
        }
    };
    private boolean visible;
    private String fragmentName;
    private transient int title;
    private transient int cardIconId;
    private transient int description;
    private transient int infoDescription;
    private transient boolean landscapeOnly =false;
    private transient RefreshablePurchaseFragment fragment;

    public DashboardComponent(String fragment) {
        this.fragmentName = fragment;
        this.visible = true;
        setupResources(fragment);
    }

    public DashboardComponent(Parcel in) {
        this.fragmentName = in.readString();
        this.visible = in.readBoolean();
        setupResources(fragmentName);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.fragmentName);
        dest.writeBoolean(this.visible);
    }

    private void setupResources(String fragmentName) {
        switch (fragmentName) {
            case "PieChartFragment" -> {
                title = R.string.pie_chart;
                cardIconId = R.mipmap.piechart;
                infoDescription = R.string.help_info_pie_chart;
                description = R.string.description_pie_chart;
            }
            case "LineChartFragment" -> {
                title = R.string.title_line_chart;
                cardIconId = R.mipmap.linechart;
                infoDescription = R.string.help_info_line_chart;
                description = R.string.description_line_chart;
                landscapeOnly = true;
            }
            case "AccumulativeChartFragment" -> {
                title = R.string.title_accumulative_line_chart;
                cardIconId = R.mipmap.linechart;
                infoDescription = R.string.help_info_accumulative_line_chart;
                description = R.string.description_accumulative_line_chart;
                landscapeOnly = true;
            }
            case "BarChartFragment" -> {
                title = R.string.title_stacked_bar_chart;
                cardIconId = R.mipmap.barchart;
                infoDescription = R.string.help_info_stacked_bar_chart;
                description = R.string.description_bar_chart;
                landscapeOnly = true;
            }
            case "MonthlyBalanceFragment" -> {
                title = R.string.title_monthly_balance;
                cardIconId = R.mipmap.monthlylimit;
                infoDescription = R.string.help_info_monthly_balance;
                description = R.string.description_monthly_balance;
                landscapeOnly = false;
            }
            case "PurchaseListPurchaseFragment" -> {
                title = R.string.title_purchases_list;
                cardIconId = R.mipmap.list;
                infoDescription = R.string.help_info_purchases_list;
                description = R.string.description_purchases_list;
            }
            default -> throw new IllegalStateException("Unexpected dashboard fragment name: " + fragmentName);
        }
    }

    @Override
    public int describeContents() {
        return this.fragmentName.hashCode();
    }

    public DashboardComponent fillFromName() {
        setupResources(this.fragmentName);
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        DashboardComponent that = (DashboardComponent) object;

        return fragmentName.equals(that.fragmentName);
    }

    @Override
    public int hashCode() {
        return fragmentName.hashCode();
    }
}
