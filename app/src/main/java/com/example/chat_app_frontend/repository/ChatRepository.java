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
                                         String channelName,
                                         String senderId,
                                         String senderName,
                                         String content,
                                         OnCompleteListener callback) {
        sendServerChannelMessage(
            serverId,
            channelId,
            channelName,
            senderId,
            senderName,
            content,
            null,
            null,
            null,
            callback
        );
        }

        public void sendServerChannelMessage(String serverId,
                         String channelId,
                         String channelName,
                         String senderId,
                         String senderName,
                         String content,
                         String replyToMessageId,
                         String replyToSenderName,
                         String replyToContent,
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
                createdAt,
                replyToMessageId,
                replyToSenderName,
                replyToContent
        );

        messagesRef.child(msgId)
                .setValue(message)
                .addOnSuccessListener(unused -> {
                    queuePushEvent(serverId, channelId, channelName, senderId, senderName, content);
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

    public void toggleMessageReaction(String serverId,
                                      String channelId,
                                      String messageId,
                                      String emoji,
                                      String uid,
                                      OnCompleteListener callback) {
        if (messageId == null || messageId.trim().isEmpty()) {
            callback.onFailure("Thiếu messageId");
            return;
        }
        if (emoji == null || emoji.trim().isEmpty()) {
            callback.onFailure("Thiếu emoji");
            return;
        }
        if (uid == null || uid.trim().isEmpty()) {
            callback.onFailure("Thiếu uid");
            return;
        }

        DatabaseReference reactionRef = getServerChannelMessagesRef(serverId, channelId)
                .child(messageId)
                .child("reactions")
                .child(emoji)
                .child(uid);

        reactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    reactionRef.removeValue()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    return;
                }

                reactionRef.setValue(true)
                        .addOnSuccessListener(unused -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public void updateMessageContent(String serverId,
                                     String channelId,
                                     String messageId,
                                     String newContent,
                                     OnCompleteListener callback) {
        if (messageId == null || messageId.trim().isEmpty()) {
            callback.onFailure("Thiếu messageId");
            return;
        }
        if (newContent == null || newContent.trim().isEmpty()) {
            callback.onFailure("Nội dung tin nhắn không hợp lệ");
            return;
        }

        getServerChannelMessagesRef(serverId, channelId)
                .child(messageId)
                .child("content")
                .setValue(newContent.trim())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteMessage(String serverId,
                              String channelId,
                              String messageId,
                              OnCompleteListener callback) {
        if (messageId == null || messageId.trim().isEmpty()) {
            callback.onFailure("Thiếu messageId");
            return;
        }

        getServerChannelMessagesRef(serverId, channelId)
                .child(messageId)
                .removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void queuePushEvent(String serverId,
                                String channelId,
                                String channelName,
                                String senderId,
                                String senderName,
                                String content) {
        DatabaseReference queueRef = FirebaseManager.getDatabaseReference("chat_push_events").push();

        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", getServerChannelTopic(serverId, channelId));
        payload.put("serverId", serverId);
        payload.put("channelId", channelId);
        payload.put("channelName", channelName);
        payload.put("senderId", senderId);
        payload.put("senderName", senderName);
        payload.put("content", content);
        payload.put("createdAt", System.currentTimeMillis());

        queueRef.setValue(payload);
    }
}
