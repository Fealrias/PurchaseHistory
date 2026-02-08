package com.fealrias.purchasehistory.data.filters;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.UUID;

import static com.fealrias.purchasehistory.data.Constants.getDefaultFilter;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseFilter implements Parcelable {
    public static final Creator<PurchaseFilter> CREATOR = new Creator<>() {
        @Override
        public PurchaseFilter createFromParcel(Parcel in) {
            return new PurchaseFilter(in);
        }

        @Override
        public PurchaseFilter[] newArray(int size) {
            return new PurchaseFilter[size];
        }
    };
    private final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private LocalDate from;
    private LocalDate to;
    private Long categoryId;
    private String categoryColor;
    private String categoryName;
    //    private PageRequest pageRequest;
    private UUID userId;

    public PurchaseFilter() {
        setFrom(LocalDate.now().withDayOfMonth(1));
        setTo(LocalDate.now());
    }

    public PurchaseFilter(LocalDate singleDay) {
        this.from = singleDay;
        this.to = singleDay;
    }

    protected PurchaseFilter(Parcel in) {
        setFrom(getDateFromParcel(in));
        setTo(getDateFromParcel(in));
        long categoryId = in.readLong();
        if (categoryId >= 0) {
            this.categoryId = categoryId;
        }
        this.categoryName = in.readString();
        this.categoryColor = in.readString();
        String userString = in.readString();
        if (userString != null && !userString.isBlank())
            setUserId(UUID.fromString(userString));
    }

    public void setCategory(CategoryView category) {
        if (category == null) {
            this.categoryId = null;
            this.categoryName = null;
            this.categoryColor = null;
            return;
        }
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.categoryColor = category.getColor();
    }


    public boolean isEmpty() {
        return from == null && to == null && categoryId == null && userId == null;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        if (from != null)
            builder.append("from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (to != null)
            builder.append("to=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (categoryId != null)
            builder.append("categoryId=").append(categoryId).append("&");
        if (userId != null)
            builder.append("userId=").append(userId).append("&");
        return builder.toString();
    }

    public String getDateString() {
        PurchaseFilter def = getDefaultFilter();
        LocalDate from = getFrom() != null ? getFrom() : def.getFrom();
        LocalDate filterTo = getTo() != null ? getTo() : def.getTo();
        return from.format(dtf) + " - " + filterTo.format(dtf);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(from == null ? -1L : from.toEpochDay());
        dest.writeLong(to == null ? -1L : to.toEpochDay());
        dest.writeLong(categoryId == null ? -1L : categoryId);
        dest.writeString(categoryName == null ? "" : categoryName);
        dest.writeString(categoryColor == null ? "" : categoryColor);
        dest.writeString(userId == null ? "" : userId.toString());
//        dest.writeParcelable(pageRequest);
    }

    private LocalDate getDateFromParcel(Parcel in) {
        long EpochDate = in.readLong();
        if (EpochDate != -1)
            return LocalDate.ofEpochDay(EpochDate);
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        PurchaseFilter that = (PurchaseFilter) object;

        if (!from.equals(that.from)) return false;
        if (!to.equals(that.to)) return false;
        if (!Objects.equals(categoryId, that.categoryId)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    public @NotNull PurchaseFilter copy() {
        PurchaseFilter purchaseFilter = new PurchaseFilter();
        purchaseFilter.setCategoryId(categoryId);
        purchaseFilter.setCategoryName(categoryName);
        purchaseFilter.setCategoryName(categoryColor);
        purchaseFilter.setFrom(from);
        purchaseFilter.setTo(to);
        purchaseFilter.setUserId(userId);
        return purchaseFilter;
    }
}
