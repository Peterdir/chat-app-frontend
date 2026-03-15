package com.example.chat_app_frontend.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.FriendRequest;
import com.example.chat_app_frontend.receiver.FriendRequestReceiver;

/**
 * Helper tạo và hiển thị notification khi nhận lời mời kết bạn mới.
 *
 * Dùng trong PendingRequestsFragment (khi observer phát hiện request mới).
 * Action buttons: "Chấp nhận" / "Từ chối" được xử lý bởi FriendRequestReceiver.
 */
public class FriendNotificationHelper {

    public static final String CHANNEL_ID   = "friend_requests";
    public static final String CHANNEL_NAME = "Lời mời kết bạn";

    /** Gọi một lần khi app khởi động (trong Application class hoặc MainActivity). */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi có người gửi lời mời kết bạn");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    /**
     * Hiển thị notification cho một lời mời kết bạn mới.
     *
     * @param context app context
     * @param request lời mời kết bạn vừa nhận được
     */
    public static void showFriendRequestNotification(Context context, FriendRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Không hiện nếu chưa có quyền
            }
        }

        int notifId = (request.getSenderId() != null)
                ? request.getSenderId().hashCode() : (int) System.currentTimeMillis();

        // Intent cho nút "Chấp nhận"
        Intent acceptIntent = new Intent(context, FriendRequestReceiver.class);
        acceptIntent.setAction(FriendRequestReceiver.ACTION_ACCEPT);
        acceptIntent.putExtra(FriendRequestReceiver.EXTRA_SENDER_ID,   request.getSenderId());
        acceptIntent.putExtra(FriendRequestReceiver.EXTRA_SENDER_NAME, request.getSenderName());
        acceptIntent.putExtra(FriendRequestReceiver.EXTRA_NOTIF_ID,    notifId);
        PendingIntent acceptPI = PendingIntent.getBroadcast(
                context, notifId,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent cho nút "Từ chối"
        Intent declineIntent = new Intent(context, FriendRequestReceiver.class);
        declineIntent.setAction(FriendRequestReceiver.ACTION_DECLINE);
        declineIntent.putExtra(FriendRequestReceiver.EXTRA_SENDER_ID,   request.getSenderId());
        declineIntent.putExtra(FriendRequestReceiver.EXTRA_SENDER_NAME, request.getSenderName());
        declineIntent.putExtra(FriendRequestReceiver.EXTRA_NOTIF_ID,    notifId);
        PendingIntent declinePI = PendingIntent.getBroadcast(
                context, notifId + 1,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String senderName = request.getSenderName() != null ? request.getSenderName() : "Ai đó";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add_friend)
                .setContentTitle("Lời mời kết bạn mới")
                .setContentText(senderName + " muốn kết bạn với bạn")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_add_friend, "Chấp nhận", acceptPI)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Từ chối", declinePI);

        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    /** Hủy notification của một sender cụ thể. */
    public static void cancelNotification(Context context, String senderId) {
        if (senderId == null) return;
        int notifId = senderId.hashCode();
        NotificationManagerCompat.from(context).cancel(notifId);
    }
}
