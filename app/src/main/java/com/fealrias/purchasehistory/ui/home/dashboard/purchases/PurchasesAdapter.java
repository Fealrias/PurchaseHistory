package com.fealrias.purchasehistory.ui.home.dashboard.purchases;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.interfaces.ViewHolder;
import com.fealrias.purchasehistorybackend.models.views.outgoing.PurchaseView;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchasesAdapter extends RecyclerView.Adapter<ViewHolder<PurchaseView>> {


    private final FragmentActivity fragmentActivity;
    @Getter
    private final List<PurchaseView> purchaseViews;
    @Setter
    private int limit = -1;

    public PurchasesAdapter(List<PurchaseView> purchaseViews, FragmentActivity fragmentActivity) {
        this.purchaseViews = purchaseViews;
        this.fragmentActivity = fragmentActivity;
    }

    public void setPurchaseViews(List<PurchaseView> purchaseViews) {
        this.purchaseViews.clear();
        this.purchaseViews.addAll(separateByMonth(purchaseViews));
        notifyDataSetChanged();
    }

    private List<PurchaseView> separateByMonth(List<PurchaseView> purchaseViews) {
        List<PurchaseView> result = new ArrayList<>();
        if (purchaseViews == null || purchaseViews.isEmpty()) return result;
        LocalDate currentMonth = purchaseViews.get(0).getTimestamp().toLocalDate().withDayOfMonth(1);
        result.add(new PurchaseViewHeader(purchaseViews.get(0)));
        for (PurchaseView purchase : purchaseViews) {
            if (!currentMonth.equals(purchase.getTimestamp().toLocalDate().withDayOfMonth(1))) {
                result.add(new PurchaseViewHeader(purchase));
                currentMonth = purchase.getTimestamp().toLocalDate().withDayOfMonth(1);
            }
            result.add(purchase);
        }
        return result;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder<PurchaseView> onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        return switch (viewType) {
            case PurchaseViewHeader.TYPE_HEADER -> {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_purchase_header, parent, false);
                yield new PurchasesHeaderViewHolder(view);
            }
            case PurchaseViewHeader.TYPE_PURCHASE -> {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_purchase, parent, false);
                yield new PurchasesViewHolder(view);
            }
            default -> throw new IllegalStateException("unsupported item type");
        };

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder<PurchaseView> holder, int position) {
        if (position >= purchaseViews.size()) return;
        PurchaseView purchaseView = purchaseViews.get(position);
        holder.bind(purchaseView, fragmentActivity.getSupportFragmentManager());
    }

    private void removePurchase(int index) {
        if (index < 0 || index >= purchaseViews.size()) return;
        this.purchaseViews.remove(index);
        new Handler(Looper.getMainLooper()).post(() -> notifyItemRemoved(index));
    }

    @Override
    public int getItemCount() {
        if (limit > 0 && limit < purchaseViews.size()) {
            return limit;
        }
        return purchaseViews.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= purchaseViews.size()) return -1;
        return PurchaseViewHeader.isHeader(purchaseViews.get(position)) ? PurchaseViewHeader.TYPE_HEADER : PurchaseViewHeader.TYPE_PURCHASE;
    }

}
