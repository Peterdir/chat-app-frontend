package com.example.chat_app_frontend.model;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private String id;
    private String name;
    private String iconUrl;
    private String ownerId;
    private String inviteCode;
    private long createdAt;
    private int iconResId; // local drawable (DM home item only)
    private boolean isSelected;

    // Required for Firebase deserialization
    public Server() {}

    // Constructor for real Firebase servers
    public Server(String id, String name, String iconUrl, String ownerId, String inviteCode) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl != null ? iconUrl : "";
        this.ownerId = ownerId;
        this.inviteCode = inviteCode;
    }

    // Legacy constructor for DM home item (local drawable)
    public Server(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("iconUrl", iconUrl != null ? iconUrl : "");
        map.put("ownerId", ownerId);
        map.put("inviteCode", inviteCode);
        map.put("createdAt", createdAt);
        return map;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getIconResId() { return iconResId; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
