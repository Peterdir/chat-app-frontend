package com.example.chat_app_frontend.model;

public class SettingsItem {
    private String title;
    private int iconRes;
    private String status;
    private boolean isNew;
    private boolean isDestructive; // For red items like Log Out

    public SettingsItem(String title, int iconRes) {
        this(title, iconRes, null, false, false);
    }

    public SettingsItem(String title, int iconRes, String status) {
        this(title, iconRes, status, false, false);
    }

    public SettingsItem(String title, int iconRes, boolean isNew) {
        this(title, iconRes, null, isNew, false);
    }

    public SettingsItem(String title, int iconRes, String status, boolean isNew, boolean isDestructive) {
        this.title = title;
        this.iconRes = iconRes;
        this.status = status;
        this.isNew = isNew;
        this.isDestructive = isDestructive;
    }

    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
    public String getStatus() { return status; }
    public boolean isNew() { return isNew; }
    public boolean isDestructive() { return isDestructive; }
}
