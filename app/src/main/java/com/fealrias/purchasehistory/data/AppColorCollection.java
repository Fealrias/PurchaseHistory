package com.fealrias.purchasehistory.data;

import android.content.Context;
import com.fealrias.purchasehistory.R;
import lombok.Getter;

@Getter
public class AppColorCollection {
    private final int foregroundColor;
    private final int backgroundColor;
    private final int middleColor;

    public AppColorCollection(Context context) {
        this.foregroundColor = context.getColor(R.color.text);
        this.backgroundColor = context.getColor(R.color.surfaceA10);
        this.middleColor = context.getColor(R.color.surfaceA30);
    }
}
