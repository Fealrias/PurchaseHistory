package com.fealrias.purchasehistory.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;

public class Category extends CategoryView implements Parcelable {
    public static final Creator<Category> CREATOR = new Creator<>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    protected Category(Parcel in) {
        setId(in.readLong());
        setName(in.readString());
        setColor(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeString(getName());
        dest.writeString(getColor());
    }
}
