package com.example.chat_app_frontend.model;

public class SettingsItem {
    private String title;
    private int iconRes;

    public SettingsItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
}
