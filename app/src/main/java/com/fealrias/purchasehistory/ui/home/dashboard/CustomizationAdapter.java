package com.fealrias.purchasehistory.ui.home.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.model.DashboardComponent;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class CustomizationAdapter extends RecyclerView.Adapter<CustomizationAdapter.ViewHolder> {

    private final List<DashboardComponent> fragments;

    public CustomizationAdapter(List<DashboardComponent> fragments) {
        this.fragments = new ArrayList<>(fragments);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardComponent fragment = fragments.get(position);
        holder.fragmentName.setText(fragment.getTitle());
        holder.fragmentIcon.setImageResource(fragment.getCardIconId());
        holder.displayCheckbox.setChecked(fragment.isVisible());
        holder.displayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> fragment.setVisible(isChecked));
        holder.buttonUp.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition > 0) {
                DashboardComponent component = fragments.remove(currentPosition);
                fragments.add(currentPosition - 1, component);
                notifyItemMoved(currentPosition, currentPosition - 1);
            }
            holder.buttonUp.setEnabled(currentPosition - 1 > 0);
        });

        holder.buttonDown.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition < fragments.size() - 1) {
                DashboardComponent component = fragments.remove(currentPosition);
                fragments.add(currentPosition + 1, component);
                notifyItemMoved(currentPosition, currentPosition + 1);
            }
            holder.buttonDown.setEnabled(currentPosition + 1 < fragments.size() - 1);
        });
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageButton buttonDown;
        final ImageButton buttonUp;
        final TextView fragmentName;
        final CheckBox displayCheckbox;
        final ImageView fragmentIcon;

        ViewHolder(View itemView) {
            super(itemView);
            fragmentName = itemView.findViewById(R.id.fragmentName);
            fragmentIcon = itemView.findViewById(R.id.fragmentIcon);
            displayCheckbox = itemView.findViewById(R.id.displayCheckbox);
            buttonUp = itemView.findViewById(R.id.buttonMoveUp);
            buttonDown = itemView.findViewById(R.id.buttonMoveDown);
        }
    }
}