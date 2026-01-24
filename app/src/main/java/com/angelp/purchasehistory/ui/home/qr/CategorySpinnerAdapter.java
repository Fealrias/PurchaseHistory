package com.angelp.purchasehistory.ui.home.qr;

import static com.angelp.purchasehistory.util.Utils.COLOR_REGEX;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<CategoryView> {
    public static final int SIZE_SMALL = 1;
    public static final int SIZE_NORMAL = 2;
    private int size = SIZE_NORMAL;

    public CategorySpinnerAdapter(@NonNull Context context, List<CategoryView> items) {
        super(context, R.layout.category_spinner_item, items);
    }
    public CategorySpinnerAdapter(@NonNull Context context,  int size, List<CategoryView> items) {
        this(context,items);
        this.size = size;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = initView(convertView, parent);
        View categoryColor = view.findViewById(R.id.categoryColor);
        TextView categoryName = view.findViewById(R.id.categoryTitle);
        CategoryView item = getItem(position);
        if (item.getId() == null) categoryColor.setVisibility(View.GONE);
        else {
            categoryColor.setVisibility(View.VISIBLE);
            String color = COLOR_REGEX.matcher(item.getColor()).find() ? item.getColor() : "#c4c4c4";
            int parsedColor = Color.parseColor(color);
            AndroidUtils.tint(categoryColor,parsedColor);
        }
        categoryName.setText(item.getName());
        if (SIZE_SMALL == size) {
            categoryName.setTextSize(11);
            view.setPadding(8,4,4,4);
            categoryColor.setLayoutParams(new LinearLayout.LayoutParams(8, 8));
            categoryName.setPadding(8,4,4,0);
        }

        return view;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private View initView(View convertView,
                          ViewGroup parent) {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_spinner_item, parent, false);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        CategoryView item = getItem(position);
        if (item == null || item.getId() == null) return Long.MIN_VALUE;
        return item.getId();
    }
}
