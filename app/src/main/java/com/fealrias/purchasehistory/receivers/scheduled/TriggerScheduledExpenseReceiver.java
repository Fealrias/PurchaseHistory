package com.fealrias.purchasehistory.receivers.scheduled;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.fealrias.purchasehistory.data.model.ScheduledNotification;
import com.fealrias.purchasehistory.web.clients.ScheduledExpenseClient;
import com.fealrias.purchasehistorybackend.models.views.incoming.TriggerPurchaseDTO;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static android.content.Context.NOTIFICATION_SERVICE;

@AndroidEntryPoint
public class TriggerScheduledExpenseReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_EXTRA_ARG = "clickedNotificationTag";
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;

    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TriggerScheduledExpenseReceiver", "Received intent");
        ScheduledNotification notification = intent.getParcelableExtra(NOTIFICATION_EXTRA_ARG);
        if (notification == null || intent.getAction() == null) {
            Log.i("TriggerScheduledExpenseReceiver", "Invalid notification or action");
            return;
        }
        if (intent.getAction().equals("trigger")) {
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            new Thread(() -> {
                scheduledExpenseClient.triggerScheduledPurchase(new TriggerPurchaseDTO(notification.getId(), LocalDateTime.now()));
                manager.cancel(notification.getId().intValue());
            }).start();
        } else {
            Log.i("TriggerScheduledExpenseReceiver", "Dismissed for ID: " + notification.getId());
        }
    }
}
