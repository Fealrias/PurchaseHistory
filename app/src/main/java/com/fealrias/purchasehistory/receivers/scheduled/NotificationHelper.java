package com.fealrias.purchasehistory.receivers.scheduled;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.model.ScheduledNotification;
import com.fealrias.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.fealrias.purchasehistory.data.Constants.Arguments.NOTIFICATION_EXTRA_ARG;

public class NotificationHelper {
    private static final String TAG = "SchaeduledExpenseNotification";
    public static final String SCHEDULED_NOTIFY = "com.fealrias.purchasehistory.SCHEDULED_NOTIFY_";

    public static void addNotificationAlarm(Context context, ScheduledExpenseView newExpense, boolean silenced) {
        if (context == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(newExpense.getId().toString(), silenced).apply();
        ScheduledNotification scheduledNotification = new ScheduledNotification(newExpense);
        reschedule(scheduledNotification, context);
    }

    public static void reschedule(ScheduledNotification notification, Context context) {
        Intent intent = new Intent(context, InitiateNotificationReceiver.class);
        ArrayList<ScheduledNotification> list = new ArrayList<>();
        list.add(notification);
        intent.putParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG, list);
        context.sendBroadcast(intent);
    }

    public static void cancelAllScheduledNotifications(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (String id : sharedPreferences.getAll().keySet()) {
            cancelNotification(context, id, am);
        }
    }

    public static void deleteAlarm(Context context, Long id) {
        if (context == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ScheduledNotification n = new ScheduledNotification(id);
        cancelNotification(context, id.toString(), am);
        Log.i(TAG, "deleteAlarm: Deleted scheduled notification for item" + n.getId());
    }

    private static void cancelNotification(Context context, String id, AlarmManager am) {
        Intent intent = new Intent(context, ScheduledNotificationReceiver.class);
        intent.setIdentifier(SCHEDULED_NOTIFY + id);
        try {
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    Integer.parseInt(id),
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) {
                Log.i(TAG, "cancelAllScheduledNotifications: Cancelling notification with id" + id);

                am.cancel(pi);
                pi.cancel();
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "cancelAllScheduledNotifications: Invalid number format in id!");
        }
    }

    public static void rescheduleNextAttempt(ScheduledNotification notification, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
        boolean isSilenced = preferences.getBoolean(notification.getId().toString(), false);

        if (isSilenced) {
            Log.i(TAG, "rescheduleNextAttempt: notification reschedule canceled. Missing or silenced.");
            return;
        }
        Log.i(TAG, "Notification is repeating.");
        long period = notification.getPeriod();
        long now = System.currentTimeMillis();
        long nextTrigger = notification.getTimestamp();
        while (nextTrigger <= now) {
            nextTrigger += period;
        }
        notification.setTimestamp(nextTrigger);
        reschedule(notification, context);
        Log.i(TAG, "reschedule: Rescheduled notification" + notification.getId() + " to " + Instant.ofEpochMilli(nextTrigger));
    }


    public static void setupAllAlarms(Context context, List<ScheduledExpenseView> scheduledExpenses) {
        Intent intent = new Intent(context, InitiateNotificationReceiver.class);
        SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);

        ArrayList<ScheduledNotification> list = new ArrayList<>();
        SharedPreferences.Editor editor = preferences.edit();
        for (ScheduledExpenseView s : scheduledExpenses) {
            ScheduledNotification notification = new ScheduledNotification(s);
            list.add(notification);
        }
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            String key = entry.getKey();
            Long id = Long.valueOf(key);
            if (scheduledExpenses.stream().noneMatch(s -> s.getId().equals(id))) {
                editor.remove(key);
                NotificationHelper.deleteAlarm(context, id);
            } else {
                boolean shouldSilence = preferences.getBoolean(key, true);
                editor.putBoolean(key, shouldSilence);
            }
        }
        editor.apply();
        intent.putParcelableArrayListExtra(NOTIFICATION_EXTRA_ARG, list);
        context.sendBroadcast(intent);

    }

    public static NotificationChannel getOrCreateChannel(NotificationManager manager, String channelId, String name) {
        NotificationChannel existing = manager.getNotificationChannel(channelId);
        if (existing != null) return existing;
        NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(name);
        manager.createNotificationChannel(channel);
        return channel;
    }
}
