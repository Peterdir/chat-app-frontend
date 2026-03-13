package com.example.chat_app_frontend.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.utils.FriendNotificationHelper;

/**
 * BroadcastReceiver xử lý action "Chấp nhận" / "Từ chối" từ notification lời mời kết bạn.
 *
 * Đăng ký trong AndroidManifest.xml với intent-filter cho 2 actions:
 *   - ACTION_ACCEPT  = "com.example.chat_app_frontend.ACCEPT_FRIEND"
 *   - ACTION_DECLINE = "com.example.chat_app_frontend.DECLINE_FRIEND"
 */
public class FriendRequestReceiver extends BroadcastReceiver {

    public static final String ACTION_ACCEPT  = "com.example.chat_app_frontend.ACCEPT_FRIEND";
    public static final String ACTION_DECLINE = "com.example.chat_app_frontend.DECLINE_FRIEND";

    public static final String EXTRA_SENDER_ID   = "sender_id";
    public static final String EXTRA_SENDER_NAME = "sender_name";
    public static final String EXTRA_NOTIF_ID    = "notif_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String senderId   = intent.getStringExtra(EXTRA_SENDER_ID);
        String senderName = intent.getStringExtra(EXTRA_SENDER_NAME);
        int    notifId    = intent.getIntExtra(EXTRA_NOTIF_ID, 0);

        if (senderId == null) return;

        // Hủy notification ngay
        FriendNotificationHelper.cancelNotification(context, senderId);

        FriendRepository friendRepo = FriendRepository.getInstance();

        if (ACTION_ACCEPT.equals(intent.getAction())) {
            friendRepo.acceptFriendRequest(senderId, new FriendRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    String name = senderName != null ? senderName : "Người dùng";
                    Toast.makeText(context,
                            "Đã chấp nhận lời mời kết bạn từ " + name,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(context,
                            "Không thể chấp nhận lời mời: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });

        } else if (ACTION_DECLINE.equals(intent.getAction())) {
            friendRepo.declineFriendRequest(senderId, new FriendRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    String name = senderName != null ? senderName : "Người dùng";
                    Toast.makeText(context,
                            "Đã từ chối lời mời từ " + name,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(context,
                            "Không thể từ chối lời mời: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
