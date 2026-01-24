package com.angelp.purchasehistory.ui.home.scheduled;

import static android.content.Context.MODE_PRIVATE;
import static com.angelp.purchasehistory.util.AndroidUtils.setNextTimestampString;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.util.CurrencyUtil;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import com.google.gson.Gson;

import java.util.List;

import lombok.Getter;

@Getter
public class ScheduledExpenseAdapter extends RecyclerView.Adapter<ScheduledExpenseAdapter.ViewHolder> {

    private final List<ScheduledExpenseView> scheduledExpenses;
    private final OnItemClickListener listener;

    public ScheduledExpenseAdapter(List<ScheduledExpenseView> scheduledExpenses, OnItemClickListener listener) {
        this.scheduledExpenses = scheduledExpenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_scheduled_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduledExpenseView scheduledExpense = scheduledExpenses.get(position);
        holder.bind(scheduledExpense, listener);
    }

    @Override
    public int getItemCount() {
        return scheduledExpenses.size();
    }

    public interface OnItemClickListener {
        void onEditClick(ScheduledExpenseView item);

        void onSilenceToggleTrigger(ScheduledExpenseView item, boolean silenced);

        void onDeleteClick(ScheduledExpenseView item);

        void onTriggerClick(ScheduledExpenseView scheduledExpense);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewPrice;
        private final TextView textViewNextDate;
        private final View viewCategoryBorder;
        private final View menuOptions;
        private final SwitchCompat silenceButton;
        private final ImageView silenceIcon;
        private final Context context;
        private final Gson gson = new Gson();


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            viewCategoryBorder = itemView.findViewById(R.id.viewCategoryBorder);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewNextDate = itemView.findViewById(R.id.textViewNextDate);
            menuOptions = itemView.findViewById(R.id.menuOptions);
            silenceButton = itemView.findViewById(R.id.silenceButton);
            silenceIcon = itemView.findViewById(R.id.silence_icon);
        }

        public void bind(ScheduledExpenseView scheduledExpense, OnItemClickListener listener) {

            SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
            boolean isSilenced = preferences.getBoolean(scheduledExpense.getId().toString(), false);


            AndroidUtils.tint(viewCategoryBorder, AndroidUtils.getColor(scheduledExpense.getCategory()));
            textViewName.setText(scheduledExpense.getNote());
            textViewPrice.setText(CurrencyUtil.formatPrice(scheduledExpense));
            setNextTimestampString(textViewNextDate, scheduledExpense);
            silenceButton.setChecked(!isSilenced);
            setSilencedState(isSilenced);
            silenceButton.setOnClickListener((v) -> {
                boolean value = silenceButton.isChecked();
                listener.onSilenceToggleTrigger(scheduledExpense, !value);
                setSilencedState(!value);
            });
            menuOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(super.itemView.getContext(), menuOptions);
                popup.inflate(R.menu.scheduled_purchase_options_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        listener.onEditClick(scheduledExpense);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        listener.onDeleteClick(scheduledExpense);
                        return true;
                    } else if (itemId == R.id.menu_trigger) {
                        listener.onTriggerClick(scheduledExpense);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        private void setSilencedState(boolean isSilenced) {
            if (isSilenced) {
                silenceIcon.setImageResource(R.drawable.bell_silent);
            } else {
                silenceIcon.setImageResource(R.drawable.bell_ring);
            }
        }


    }
}