package com.fealrias.purchasehistory.ui.home.dashboard.purchases;

import com.fealrias.purchasehistorybackend.models.views.outgoing.PurchaseView;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class PurchaseViewHeader extends PurchaseView {
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_PURCHASE = 2;
    private final String title;
    private final PurchaseView purchaseView;

    public PurchaseViewHeader(PurchaseView purchase) {
        super();
        this.purchaseView = purchase;
        this.title = purchase.getTimestamp().format(DateTimeFormatter.ofPattern("MMM y"));
    }

    public static boolean isHeader(PurchaseView view) {
        return view instanceof PurchaseViewHeader;
    }

}
