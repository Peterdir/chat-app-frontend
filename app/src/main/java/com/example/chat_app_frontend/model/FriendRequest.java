package com.example.chat_app_frontend.model;

/**
 * Model cho lời mời kết bạn.
 *
 * Firebase RTDB path:
 *   friend_requests/{receiverUid}/{senderUid}/
 *
 * Constructor rỗng bắt buộc để Firebase có thể deserialize.
 */
public class FriendRequest {

    private String senderId;
    private String senderName;
    private String senderAvatarUrl;
    private long   timestamp;
    private String status; // "pending" | "accepted" | "declined"

    /** Bắt buộc cho Firebase */
    public FriendRequest() {}

    public FriendRequest(String senderId, String senderName, String senderAvatarUrl) {
        this.senderId        = senderId;
        this.senderName      = senderName;
        this.senderAvatarUrl = senderAvatarUrl;
        this.timestamp       = System.currentTimeMillis();
        this.status          = "pending";
    }

    // --- Getters & Setters ---

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
