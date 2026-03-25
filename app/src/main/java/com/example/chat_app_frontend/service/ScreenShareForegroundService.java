package com.example.chat_app_frontend.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.chat_app_frontend.R;

public class ScreenShareForegroundService extends Service {
    public interface ForegroundReadyListener {
        void onForegroundReady();
    }

    private static final String CHANNEL_ID = "screen_share_channel";
    private static final int NOTIFICATION_ID = 4102;
    private static final String ACTION_START = "com.example.chat_app_frontend.action.START_SCREEN_SHARE";
    private static final String ACTION_STOP = "com.example.chat_app_frontend.action.STOP_SCREEN_SHARE";

    private static volatile ForegroundReadyListener foregroundReadyListener;

    public static Intent createStartIntent(Context context) {
        Intent intent = new Intent(context, ScreenShareForegroundService.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent createStopIntent(Context context) {
        Intent intent = new Intent(context, ScreenShareForegroundService.class);
        intent.setAction(ACTION_STOP);
        return intent;
    }

    public static void setForegroundReadyListener(@Nullable ForegroundReadyListener listener) {
        foregroundReadyListener = listener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        createNotificationChannel();
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            );
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        ForegroundReadyListener listener = foregroundReadyListener;
        foregroundReadyListener = null;
        if (listener != null) {
            listener.onForegroundReady();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_videocam)
                .setContentTitle("Dang chia se man hinh")
                .setContentText("Ban co the mo ung dung khac trong luc chia se.")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Screen sharing",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Thong bao khi dang chia se man hinh");
        notificationManager.createNotificationChannel(channel);
    }
}
