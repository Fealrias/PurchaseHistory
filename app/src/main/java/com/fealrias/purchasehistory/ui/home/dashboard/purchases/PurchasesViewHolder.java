package com.fealrias.purchasehistory.ui.home.dashboard.purchases;

import static com.fealrias.purchasehistory.data.Constants.Arguments.PURCHASE_EDIT_DIALOG_CONTENT_KEY;
import static com.fealrias.purchasehistory.data.Constants.Arguments.PURCHASE_EDIT_DIALOG_ID_KEY;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.fealrias.purchasehistory.data.interfaces.ViewHolder;
import com.fealrias.purchasehistory.data.model.parcel.PurchaseParcel;
import com.fealrias.purchasehistory.databinding.RecyclerViewPurchaseBinding;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistorybackend.models.views.outgoing.PurchaseView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import lombok.Getter;

@Getter
public class PurchasesViewHolder extends ViewHolder<PurchaseView> {
    private final String TAG = this.getClass().getSimpleName();
    private final DateTimeFormatter readableFormatter = DateTimeFormatter.ofPattern("dd.MM.yy hh:mm:ss");

    final RecyclerViewPurchaseBinding binding;
    private PurchaseEditDialog editDialog;
    @Inject
    PurchaseFilterSingleton purchaseFilter;
    private FragmentManager fragmentManager;

    public PurchasesViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        binding = RecyclerViewPurchaseBinding.bind(itemView);
    }

    public void bind(PurchaseView purchaseView, FragmentManager fragmentManager) {
        editDialog = new PurchaseEditDialog();
        this.fragmentManager = fragmentManager;
        if (purchaseView.getPrice() != null)
            binding.purchasePriceText.setText(AndroidUtils.formatCurrency(purchaseView.getPrice(), purchaseView.getCurrency()));
        else binding.purchasePriceText.setText("-");
        if (purchaseView.getNote() != null) {
            binding.purchaseNoteText.setText(purchaseView.getNote());
        } else binding.purchaseNoteText.setText("");
        if (purchaseView.getTimestamp() != null) {
            long epochMilli = purchaseView.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            CharSequence timeString = DateUtils.getRelativeTimeSpanString(epochMilli);
            binding.purchaseTimeText.setText(timeString);
        } else binding.purchaseTimeText.setText("-");

        if (purchaseView.getCategory() != null) {
            binding.purchaseCategoryText.setVisibility(View.VISIBLE);
            binding.purchaseCategoryText.setText(purchaseView.getCategory().getName().toUpperCase());
            int color;
            try {
                color = Color.parseColor(purchaseView.getCategory().getColor().toUpperCase());
            } catch (IllegalArgumentException e) {
                color = Color.GRAY;
            }
            int textColor = AndroidUtils.getTextColor(color);

            Drawable background = binding.purchaseCategoryText.getBackground().mutate();
            background.setColorFilter(new BlendModeColorFilter(color, BlendMode.COLOR));
            binding.purchaseCategoryText.setTextColor(textColor);
        } else {
            binding.purchaseCategoryText.setVisibility(View.INVISIBLE);
        }
        if (purchaseView.getTimestamp() != null) {
            binding.purchaseEditButton.setEnabled(true);
            binding.purchaseEditButton.setOnClickListener((v) -> {
                if (purchaseView.getId() != null) {
                    PurchaseParcel purchaseDTO = generatePurchaseDTO(purchaseView);
                    Bundle bundle = new Bundle();
                    bundle.putLong(PURCHASE_EDIT_DIALOG_ID_KEY, purchaseView.getId());
                    bundle.putParcelable(PURCHASE_EDIT_DIALOG_CONTENT_KEY, purchaseDTO);
                    editDialog.setArguments(bundle);
                    editDialog.show(fragmentManager, "editDialog" + purchaseView.getBillId());
                    editDialog.setOnSuccess((newView) -> {
                        if (newView != null) {
                            this.bind(newView, fragmentManager);
                        }

                    });
                } else PurchaseHistoryApplication.getInstance().alert("Purchase does not have an id");

            });
        } else binding.purchaseEditButton.setEnabled(false);


    }

    private PurchaseParcel generatePurchaseDTO(PurchaseView purchaseView) {
        PurchaseParcel purchaseDTO = new PurchaseParcel();
        purchaseDTO.setQrContent(purchaseView.getQrContent());
        purchaseDTO.setPrice(purchaseView.getPrice());
        purchaseDTO.setTimestamp(purchaseView.getTimestamp());
        purchaseDTO.setBillId(purchaseView.getBillId());
        purchaseDTO.setStoreId(purchaseView.getStoreId());
        purchaseDTO.setNote(purchaseView.getNote());
        purchaseDTO.setCurrency(purchaseView.getCurrency());
        if (purchaseView.getCategory() != null) purchaseDTO.setCategoryId(purchaseView.getCategory().getId());
        return purchaseDTO;
    }

}
