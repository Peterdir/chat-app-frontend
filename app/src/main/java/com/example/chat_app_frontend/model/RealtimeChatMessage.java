package com.example.chat_app_frontend.model;

public class RealtimeChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private String serverId;
    private String channelId;
    private long createdAt;

    public RealtimeChatMessage() {
        // Required empty constructor for Firebase
    }

    public RealtimeChatMessage(String id, String senderId, String senderName, String content,
                               String serverId, String channelId, long createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.serverId = serverId;
        this.channelId = channelId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
