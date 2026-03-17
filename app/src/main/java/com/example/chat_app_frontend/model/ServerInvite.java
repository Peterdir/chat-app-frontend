package com.example.chat_app_frontend.model;

/**
 * Đại diện cho một lời mời vào server.
 *
 * Firebase path (được ghi kép):
 *   server_invites/{serverId}/{friendUid}/
 *   user_invites/{friendUid}/{serverId}/     ← để query nhanh "tất cả lời mời của tôi"
 */
public class ServerInvite {
    private String serverId;
    private String serverName;
    private String invitedByUid;
    private String invitedByName;
    private String friendUid;
    private String friendName;
    private String friendAvatarUrl;
    private long invitedAt;
    private String status; // "pending", "accepted", "declined"

    public ServerInvite() {}

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getInvitedByUid() { return invitedByUid; }
    public void setInvitedByUid(String invitedByUid) { this.invitedByUid = invitedByUid; }

    public String getInvitedByName() { return invitedByName; }
    public void setInvitedByName(String invitedByName) { this.invitedByName = invitedByName; }

    public String getFriendUid() { return friendUid; }
    public void setFriendUid(String friendUid) { this.friendUid = friendUid; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getFriendAvatarUrl() { return friendAvatarUrl; }
    public void setFriendAvatarUrl(String friendAvatarUrl) { this.friendAvatarUrl = friendAvatarUrl; }

    public long getInvitedAt() { return invitedAt; }
    public void setInvitedAt(long invitedAt) { this.invitedAt = invitedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
