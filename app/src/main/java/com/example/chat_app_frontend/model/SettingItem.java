package com.example.chat_app_frontend.model;

public class SettingItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public int type;
    public String title;
    public int iconRes;

    // Constructor
    public SettingItem(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
    }

    // Constructor
    public SettingItem(String title, int iconRes) {
        this.type = TYPE_ITEM;
        this.title = title;
        this.iconRes = iconRes;
    }
}