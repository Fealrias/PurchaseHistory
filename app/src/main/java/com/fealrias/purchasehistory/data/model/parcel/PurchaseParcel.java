package com.fealrias.purchasehistory.data.model.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fealrias.purchasehistorybackend.models.views.incoming.PurchaseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseParcel extends PurchaseDTO implements Parcelable {
    public PurchaseParcel() {
    }

    public PurchaseParcel(PurchaseDTO purchaseDTO) {
        super(purchaseDTO.getQrContent(),
                purchaseDTO.getPrice(),
                purchaseDTO.getTimestamp(),
                purchaseDTO.getBillId(),
                purchaseDTO.getStoreId(),
                purchaseDTO.getCategoryId(),
                purchaseDTO.getNote(),
                purchaseDTO.getCurrency());
    }

    protected PurchaseParcel(Parcel in) {
        setQrContent(in.readString());

        // BigDecimal is not Parcelable, write/read as String
        String priceString = in.readString();
        setPrice(priceString != null ? new BigDecimal(priceString) : null);

        // LocalDateTime is not Parcelable, convert to String with ISO format
        String timestampString = in.readString();
        setTimestamp(timestampString != null ? LocalDateTime.parse(timestampString) : null);

        setBillId(in.readString());
        setStoreId(in.readString());

        if (in.readByte() != 0) {
            setCategoryId(in.readLong());
        }

        setNote(in.readString());
        setCurrency(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int i) {
        dest.writeString(getQrContent());

        // Write BigDecimal as String
        dest.writeString(getPrice() != null ? getPrice().toPlainString() : null);

        // Write LocalDateTime as ISO String
        dest.writeString(getTimestamp() != null ? getTimestamp().toString() : null);

        dest.writeString(getBillId());
        dest.writeString(getStoreId());

        if (getCategoryId() == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(getCategoryId());
        }

        dest.writeString(getNote());
        dest.writeString(getCurrency());
    }

    public static final Creator<PurchaseParcel> CREATOR = new Creator<>() {
        @Override
        public PurchaseParcel createFromParcel(Parcel in) {
            return new PurchaseParcel(in);
        }

        @Override
        public PurchaseParcel[] newArray(int size) {
            return new PurchaseParcel[size];
        }
    };
}
