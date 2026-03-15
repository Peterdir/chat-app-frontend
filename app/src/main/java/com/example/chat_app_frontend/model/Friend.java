package com.example.chat_app_frontend.model;

/**
 * Model bạn bè.
 *
 * Firebase RTDB path:
 *   friends/{uid}/{friendUid}/
 *     friendName, friendAvatarUrl, since
 *
 * Note: friendUid được lấy từ snapshot key, không lưu làm field riêng.
 */
public class Friend {

    // UID lấy từ snapshot key khi đọc Firebase
    private String uid;

    // Fields lưu trong Firebase
    private String friendName;
    private String friendAvatarUrl;
    private long   since; // Unix timestamp khi kết bạn

    // Status chỉ dùng trong UI (lấy realtime từ users/{uid}/status)
    private String onlineStatus;

    // Giữ lại avatarResId để tương thích với DMFragment (mock data)
    private int avatarResId;

    /** Bắt buộc cho Firebase */
    public Friend() {}

    /** Constructor dùng với Firebase data */
    public Friend(String uid, String friendName, String friendAvatarUrl, long since) {
        this.uid             = uid;
        this.friendName      = friendName;
        this.friendAvatarUrl = friendAvatarUrl;
        this.since           = since;
    }

    /** Constructor cũ cho mock data trong DMFragment */
    public Friend(String id, String name, String status, int avatarResId) {
        this.uid          = id;
        this.friendName   = name;
        this.onlineStatus = status;
        this.avatarResId  = avatarResId;
    }

    // --- Getters & Setters ---

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getFriendAvatarUrl() { return friendAvatarUrl; }
    public void setFriendAvatarUrl(String friendAvatarUrl) { this.friendAvatarUrl = friendAvatarUrl; }

    public long getSince() { return since; }
    public void setSince(long since) { this.since = since; }

    public String getOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(String onlineStatus) { this.onlineStatus = onlineStatus; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }

    // Helpers for backward-compat
    public String getId()   { return uid; }
    public String getName() { return friendName; }
    public String getStatus() { return onlineStatus; }
}
