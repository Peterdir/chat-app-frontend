package com.example.chat_app_frontend.model;

public class Message {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_DATE_DIVIDER = "date";

    private String id;
    private String senderId;
    private String senderName;
    private int senderAvatarResId;
    private String content;
    private String timestamp;      // e.g. "09/01/2026 22:38"
    private String dateLabel;      // e.g. "9 tháng 1 năm 2026" – used for DATE_DIVIDER
    private String messageType;
    private int imageResId;
    private boolean isFirstInGroup; // first msg of consecutive msgs from same sender
    private boolean isSelf;         // sent by current user

    // Constructor for date divider
    public Message(String dateLabel) {
        this.messageType = TYPE_DATE_DIVIDER;
        this.dateLabel = dateLabel;
    }

    // Constructor for text message
    public Message(String id, String senderId, String senderName,
                   int senderAvatarResId, String content,
                   String timestamp, boolean isSelf, boolean isFirstInGroup) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatarResId = senderAvatarResId;
        this.content = content;
        this.timestamp = timestamp;
        this.messageType = TYPE_TEXT;
        this.isSelf = isSelf;
        this.isFirstInGroup = isFirstInGroup;
    }

    // Constructor for image message
    public Message(String id, String senderId, String senderName,
                   int senderAvatarResId, String content,
                   String timestamp, boolean isSelf, boolean isFirstInGroup,
                   int imageResId) {
        this(id, senderId, senderName, senderAvatarResId, content,
                timestamp, isSelf, isFirstInGroup);
        this.messageType = TYPE_IMAGE;
        this.imageResId = imageResId;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public int getSenderAvatarResId() { return senderAvatarResId; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public String getDateLabel() { return dateLabel; }
    public String getMessageType() { return messageType; }
    public int getImageResId() { return imageResId; }
    public boolean isFirstInGroup() { return isFirstInGroup; }
    public boolean isSelf() { return isSelf; }

    public void setFirstInGroup(boolean firstInGroup) { isFirstInGroup = firstInGroup; }
}
