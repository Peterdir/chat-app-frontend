package com.example.chat_app_frontend.model;

public class Notification {
    private int avatarResId;
    private String message;
    private String timeAgo;
    private String buttonText; // null nếu không có nút
    public Notification(int avatarResId, String message, String timeAgo, String buttonText) {
        this.avatarResId = avatarResId;
        this.message = message;
        this.timeAgo = timeAgo;
        this.buttonText = buttonText;
    }
    public int getAvatarResId() { return avatarResId; }
    public String getMessage() { return message; }
    public String getTimeAgo() { return timeAgo; }
    public String getButtonText() { return buttonText; }
    public boolean hasButton() { return buttonText != null; }
}
