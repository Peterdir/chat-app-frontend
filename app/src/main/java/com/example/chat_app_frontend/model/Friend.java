package com.example.chat_app_frontend.model;

public class Friend {
    private String id;
    private String name;
    private String status;
    private int avatarResId;

    public Friend(String id, String name, String status, int avatarResId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.avatarResId = avatarResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}
