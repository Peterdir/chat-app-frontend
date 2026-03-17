package com.example.chat_app_frontend.repository;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.RealtimeChatMessage;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    public interface OnMessagesChangedListener {
        void onSuccess(List<RealtimeChatMessage> messages);
        void onFailure(String error);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    private static ChatRepository instance;

    private ChatRepository() {
    }

    public static synchronized ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    public ValueEventListener observeServerChannelMessages(String serverId,
                                                           String channelId,
                                                           OnMessagesChangedListener callback) {
        DatabaseReference ref = getServerChannelMessagesRef(serverId, channelId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RealtimeChatMessage> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    RealtimeChatMessage msg = child.getValue(RealtimeChatMessage.class);
                    if (msg == null) {
                        continue;
                    }
                    if (msg.getId() == null || msg.getId().trim().isEmpty()) {
                        msg.setId(child.getKey());
                    }
                    messages.add(msg);
                }
                callback.onSuccess(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };
        ref.orderByChild("createdAt").limitToLast(200).addValueEventListener(listener);
        return listener;
    }

    public void removeServerChannelObserver(String serverId, String channelId, ValueEventListener listener) {
        if (listener == null) {
            return;
        }
        getServerChannelMessagesRef(serverId, channelId).removeEventListener(listener);
    }

    public void sendServerChannelMessage(String serverId,
                                         String channelId,
                                         String senderId,
                                         String senderName,
                                         String content,
                                         OnCompleteListener callback) {
        DatabaseReference messagesRef = getServerChannelMessagesRef(serverId, channelId);
        String msgId = messagesRef.push().getKey();
        if (msgId == null) {
            callback.onFailure("Không thể tạo ID tin nhắn");
            return;
        }

        long createdAt = System.currentTimeMillis();
        RealtimeChatMessage message = new RealtimeChatMessage(
                msgId,
                senderId,
                senderName,
                content,
                serverId,
                channelId,
                createdAt
        );

        messagesRef.child(msgId)
                .setValue(message)
                .addOnSuccessListener(unused -> {
                    queuePushEvent(serverId, channelId, senderId, senderName, content);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void subscribeToServerChannelTopic(String serverId, String channelId) {
        FirebaseMessaging.getInstance().subscribeToTopic(getServerChannelTopic(serverId, channelId));
    }

    public static String getServerChannelTopic(String serverId, String channelId) {
        String safeServerId = sanitizeTopicPart(serverId);
        String safeChannelId = sanitizeTopicPart(channelId);
        return "server_" + safeServerId + "_channel_" + safeChannelId;
    }

    private static String sanitizeTopicPart(String value) {
        if (value == null) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9-_.~%]", "_");
    }

    private DatabaseReference getServerChannelMessagesRef(String serverId, String channelId) {
        return FirebaseManager.getDatabaseReference("chat_messages/server_channels")
                .child(serverId + "_" + channelId)
                .child("messages");
    }

    private void queuePushEvent(String serverId,
                                String channelId,
                                String senderId,
                                String senderName,
                                String content) {
        DatabaseReference queueRef = FirebaseManager.getDatabaseReference("chat_push_events").push();

        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", getServerChannelTopic(serverId, channelId));
        payload.put("serverId", serverId);
        payload.put("channelId", channelId);
        payload.put("senderId", senderId);
        payload.put("senderName", senderName);
        payload.put("content", content);
        payload.put("createdAt", System.currentTimeMillis());

        queueRef.setValue(payload);
    }
}
