package com.example.chat_app_frontend.model;

import java.util.Map;

public class RealtimeChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private String serverId;
    private String channelId;
    private long createdAt;
    private Map<String, Map<String, Boolean>> reactions;
    private String replyToMessageId;
    private String replyToSenderName;
    private String replyToContent;

    public RealtimeChatMessage() {
        // Required empty constructor for Firebase
    }

    public RealtimeChatMessage(String id, String senderId, String senderName, String content,
                               String serverId, String channelId, long createdAt) {
        this(id, senderId, senderName, content, serverId, channelId, createdAt,
                null, null, null);
    }

    public RealtimeChatMessage(String id, String senderId, String senderName, String content,
                               String serverId, String channelId, long createdAt,
                               String replyToMessageId, String replyToSenderName, String replyToContent) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.serverId = serverId;
        this.channelId = channelId;
        this.createdAt = createdAt;
        this.replyToMessageId = replyToMessageId;
        this.replyToSenderName = replyToSenderName;
        this.replyToContent = replyToContent;
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

    public Map<String, Map<String, Boolean>> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Map<String, Boolean>> reactions) {
        this.reactions = reactions;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getReplyToSenderName() {
        return replyToSenderName;
    }

    public void setReplyToSenderName(String replyToSenderName) {
        this.replyToSenderName = replyToSenderName;
    }

    public String getReplyToContent() {
        return replyToContent;
    }

    public void setReplyToContent(String replyToContent) {
        this.replyToContent = replyToContent;
    }
}
