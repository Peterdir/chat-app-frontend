package com.example.chat_app_frontend.model;

public class Server {
    private String id;
    private String name;
    private int iconResId; // Using resource ID for mock data, or could be URL
    private boolean isSelected;

    public Server(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
