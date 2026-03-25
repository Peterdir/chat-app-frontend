package com.example.chat_app_frontend.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.ui.DMChatActivity;
import com.example.chat_app_frontend.ui.MainActivity;
import com.example.chat_app_frontend.ui.PrivateCallActivity;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_messages";
    private static final String CALL_CHANNEL_ID = "incoming_calls";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null || uid.trim().isEmpty()) {
            return;
        }
        FirebaseManager.getDatabaseReference("user_fcm_tokens")
                .child(uid)
                .child(token)
                .setValue(true);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String eventType = data != null ? data.get("eventType") : null;
        if ("call_invite".equals(eventType)) {
            handleIncomingCall(data);
            return;
        }

        createNotificationChannel();

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if ((title == null || title.trim().isEmpty()) && data != null) {
            title = data.get("title");
        }
        if ((body == null || body.trim().isEmpty()) && data != null) {
            body = data.get("body");
        }

        if (title == null || title.trim().isEmpty()) {
            title = "Tin nhan moi";
        }
        if (body == null || body.trim().isEmpty()) {
            body = "Ban vua nhan duoc tin nhan.";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String serverId = data != null ? data.get("serverId") : null;
        String channelId = data != null ? data.get("channelId") : null;
        String channelName = data != null ? data.get("channelName") : null;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.EXTRA_OPEN_SERVER_ID, serverId);
        intent.putExtra(MainActivity.EXTRA_OPEN_CHANNEL_ID, channelId);
        intent.putExtra(MainActivity.EXTRA_OPEN_CHANNEL_NAME, channelName);

        Uri deepLinkUri = Uri.parse("chatapp://server-channel"
            + "?serverId=" + safe(serverId)
            + "&channelId=" + safe(channelId)
            + "&channelName=" + safe(channelName));
        intent.setData(deepLinkUri);

        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingFlags);

        int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
            .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Chat messages",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void createCallNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CALL_CHANNEL_ID,
                "Incoming calls",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Thông báo cuộc gọi đến");
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void handleIncomingCall(Map<String, String> data) {
        createCallNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String friendName = data != null ? data.get("callerName") : "Friend";
        String friendUid = data != null ? data.get("callerUid") : null;
        boolean isVideo = data != null && "video".equalsIgnoreCase(data.get("callType"));

        Intent intent = new Intent(this, PrivateCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_NAME, friendName);
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, friendUid);
        intent.putExtra("is_video", isVideo);
        intent.putExtra("is_caller", false);

        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, pendingFlags);

        int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        String callText = isVideo ? "Cuộc gọi video đến" : "Cuộc gọi thoại đến";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CALL_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(friendName != null ? friendName : "Friend")
                .setContentText(callText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }

    private String safe(String value) {
        return value == null ? "" : Uri.encode(value);
    }
}
