package com.angelp.purchasehistory.data.model;

import android.app.AlarmManager;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistorybackend.models.enums.ScheduledPeriod;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledNotification implements Parcelable {
    public static final Creator<ScheduledNotification> CREATOR = new Creator<>() {
        @Override
        public ScheduledNotification createFromParcel(Parcel in) {
            return new ScheduledNotification(in);
        }

        @Override
        public ScheduledNotification[] newArray(int size) {
            return new ScheduledNotification[size];
        }
    };
    private Long id;
    private long timestamp;
    private long period;
    private BigDecimal price;
    private Long categoryId;
    private int color;
    private Boolean enabled;
    private String currency;
    private String note;

    public ScheduledNotification(Long id) {
        this.id = id;
    }
    public ScheduledNotification(ScheduledExpenseView expense) {
        this.id = expense.getId();
        this.timestamp = getEpochMilli(expense);
        this.price = expense.getPrice();
        this.period = getInterval(expense.getPeriod());
        this.categoryId = expense.getCategory() == null ? null : expense.getCategory().getId();
        this.color = expense.getCategory() == null ? Color.GRAY : AndroidUtils.getColor(expense.getCategory());
        this.enabled = expense.isEnabled();
        this.currency = expense.getCurrency();
        this.note = expense.getNote();
    }

    protected ScheduledNotification(Parcel in) {
        price = new BigDecimal(in.readString());
        note = in.readString();
        long l = in.readLong();
        categoryId = l == -1 ? null : l;
        color = in.readInt();
        timestamp = in.readLong();
        period = in.readLong();
        id = in.readLong();
        currency = in.readString();
        enabled = in.readByte() != 0;
    }

    private static long getInterval(ScheduledPeriod period) {
        return switch (period) {
            case DAILY -> AlarmManager.INTERVAL_DAY;
            case WEEKLY -> AlarmManager.INTERVAL_DAY * 7;
            case MONTHLY -> AlarmManager.INTERVAL_DAY * 30;
            case YEARLY -> AlarmManager.INTERVAL_DAY * 365;
            case NEVER -> -1L;
        };
    }

    private static long getEpochMilli(ScheduledExpenseView notification) {
        if (notification.getNextTimestamp() == null) {
            return 0;
        }
        return notification.getNextTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public boolean isRepeating() {
        return period != -1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price.toString());
        dest.writeString(note);
        dest.writeLong(categoryId == null ? -1 : categoryId);
        dest.writeInt(color);
        dest.writeLong(timestamp);
        dest.writeLong(period);
        dest.writeLong(id);
        dest.writeString(currency);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public PurchaseDTO getPurchaseDTO() {
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setNote(this.note);
        purchaseDTO.setPrice(this.price);
        purchaseDTO.setCategoryId(this.categoryId);
        purchaseDTO.setTimestamp(LocalDateTime.now());
        purchaseDTO.setCurrency(this.currency);
        return purchaseDTO;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ScheduledNotification that = (ScheduledNotification) object;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
