package com.example.chat_app_frontend.model;

public class FriendSuggestion {
    private int avatarResId;
    private String displayName;
    private String username;
    public FriendSuggestion(int avatarResId, String displayName, String username) {
        this.avatarResId = avatarResId;
        this.displayName = displayName;
        this.username = username;
    }
    public int getAvatarResId() { return avatarResId; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
}
