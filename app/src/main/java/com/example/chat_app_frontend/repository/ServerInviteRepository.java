package com.example.chat_app_frontend.repository;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.model.ServerInvite;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository quản lý lời mời vào server.
 *
 * Firebase schema:
 *   server_invites/{serverId}/{friendUid}/  — tra cứu theo server
 *   user_invites/{uid}/{serverId}/          — tra cứu nhanh theo người dùng (ghi kép)
 *
 * Mỗi node chứa: friendUid, friendName, friendAvatarUrl,
 *                serverId, serverName, invitedByUid, invitedByName, invitedAt, status
 */
public class ServerInviteRepository {

    private static final String SERVER_INVITES_PATH = "server_invites";

    private static ServerInviteRepository instance;
    private final DatabaseReference dbRef;

    private ServerInviteRepository() {
        dbRef = FirebaseManager.getDatabase().getReference();
    }

    public static synchronized ServerInviteRepository getInstance() {
        if (instance == null) {
            instance = new ServerInviteRepository();
        }
        return instance;
    }

    private String currentUid() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    public void getMyInvitedFriendIds(String serverId, OnInvitedIdsListener callback) {
        String myUid = currentUid();
        if (myUid == null) {
            if (callback != null) {
                callback.onFailure("Chưa đăng nhập");
            }
            return;
        }

        dbRef.child(SERVER_INVITES_PATH).child(serverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Set<String> invitedIds = new HashSet<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String invitedByUid = child.child("invitedByUid").getValue(String.class);
                            String friendUid = child.child("friendUid").getValue(String.class);
                            String status = child.child("status").getValue(String.class);
                            if (myUid.equals(invitedByUid)
                                    && friendUid != null
                                    && !friendUid.isEmpty()
                                    && "pending".equals(status)) {
                                invitedIds.add(friendUid);
                            }
                        }
                        if (callback != null) {
                            callback.onLoaded(invitedIds);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) {
                            callback.onFailure(error.getMessage());
                        }
                    }
                });
    }

    public void sendServerInvite(String serverId, String serverName, Friend friend, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) {
            if (callback != null) callback.onFailure("Chưa đăng nhập");
            return;
        }
        if (friend == null || friend.getUid() == null || friend.getUid().isEmpty()) {
            if (callback != null) callback.onFailure("Không xác định được người bạn để mời");
            return;
        }

        String friendUid = friend.getUid();
        long now = System.currentTimeMillis();

        // Lấy displayName của người mời trước khi ghi
        FirebaseManager.getUsersRef().child(myUid).child("displayName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        String inviterName = snap.getValue(String.class);
                        if (inviterName == null || inviterName.isEmpty()) inviterName = "Ai đó";
                        writeInvite(serverId, serverName, friendUid, friend, myUid, inviterName, now, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        // Ghi với tên dự phòng nếu không lấy được
                        writeInvite(serverId, serverName, friendUid, friend, myUid, "Ai đó", now, callback);
                    }
                });
    }

    private void writeInvite(String serverId, String serverName, String friendUid,
                             Friend friend, String myUid, String inviterName,
                             long now, OnCompleteListener callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("friendUid", friendUid);
        data.put("friendName", friend.getFriendName());
        data.put("friendAvatarUrl", friend.getFriendAvatarUrl());
        data.put("serverId", serverId);
        data.put("serverName", serverName);
        data.put("invitedByUid", myUid);
        data.put("invitedByName", inviterName);
        data.put("invitedAt", now);
        data.put("status", "pending");

        Map<String, Object> updates = new HashMap<>();
        // Path 1: tra cứu theo server (dùng để check đã mời chưa)
        String p1 = "server_invites/" + serverId + "/" + friendUid;
        // Path 2: tra cứu theo người được mời (dùng để hiện thông báo)
        String p2 = "user_invites/" + friendUid + "/" + serverId;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            updates.put(p1 + "/" + entry.getKey(), entry.getValue());
            updates.put(p2 + "/" + entry.getKey(), entry.getValue());
        }

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /**
     * Người mời hủy lời mời đang pending của một bạn cụ thể.
     */
    public void cancelServerInvite(String serverId, String friendUid, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) {
            if (callback != null) callback.onFailure("Chưa đăng nhập");
            return;
        }
        if (serverId == null || serverId.trim().isEmpty() || friendUid == null || friendUid.trim().isEmpty()) {
            if (callback != null) callback.onFailure("Thiếu thông tin để hủy lời mời");
            return;
        }

        DatabaseReference inviteRef = dbRef.child("server_invites").child(serverId).child(friendUid);
        inviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (callback != null) callback.onSuccess();
                    return;
                }

                String invitedByUid = snapshot.child("invitedByUid").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                if (!myUid.equals(invitedByUid)) {
                    if (callback != null) callback.onFailure("Bạn không có quyền hủy lời mời này");
                    return;
                }
                if (!"pending".equals(status)) {
                    if (callback != null) callback.onFailure("Lời mời không còn ở trạng thái chờ");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("server_invites/" + serverId + "/" + friendUid, null);
                updates.put("user_invites/" + friendUid + "/" + serverId, null);
                dbRef.updateChildren(updates)
                        .addOnSuccessListener(v -> {
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Đọc lời mời đang chờ của current user (người được mời)
    // -------------------------------------------------------------------------

    /**
     * Lấy tất cả lời mời server đang "pending" mà current user nhận được.
     * Đọc từ user_invites/{currentUid}/.
     */
    public void getPendingInvitesForMe(OnInviteListListener callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        dbRef.child("user_invites").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ServerInvite> result = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String status = child.child("status").getValue(String.class);
                            if (!"pending".equals(status)) continue;

                            ServerInvite invite = new ServerInvite();
                            invite.setServerId(child.child("serverId").getValue(String.class));
                            invite.setServerName(child.child("serverName").getValue(String.class));
                            invite.setInvitedByUid(child.child("invitedByUid").getValue(String.class));
                            invite.setInvitedByName(child.child("invitedByName").getValue(String.class));
                            invite.setFriendUid(uid);
                            Long ts = child.child("invitedAt").getValue(Long.class);
                            invite.setInvitedAt(ts != null ? ts : 0);
                            invite.setStatus("pending");
                            if (invite.getServerId() != null && invite.getServerName() != null) {
                                result.add(invite);
                            }
                        }
                        if (!result.isEmpty()) {
                            callback.onSuccess(result);
                            return;
                        }

                        // Fallback cho dữ liệu cũ/chưa mirror sang user_invites.
                        loadPendingInvitesFromServerNode(uid, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    private void loadPendingInvitesFromServerNode(String uid, OnInviteListListener callback) {
        dbRef.child(SERVER_INVITES_PATH)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ServerInvite> result = new ArrayList<>();
                        for (DataSnapshot serverNode : snapshot.getChildren()) {
                            String serverId = serverNode.getKey();
                            if (serverId == null) {
                                continue;
                            }

                            DataSnapshot inviteNode = serverNode.child(uid);
                            if (!inviteNode.exists()) {
                                continue;
                            }

                            String status = inviteNode.child("status").getValue(String.class);
                            if (!"pending".equals(status)) {
                                continue;
                            }

                            ServerInvite invite = new ServerInvite();
                            invite.setServerId(serverId);
                            String serverName = inviteNode.child("serverName").getValue(String.class);
                            invite.setServerName(serverName);
                            invite.setInvitedByUid(inviteNode.child("invitedByUid").getValue(String.class));
                            invite.setInvitedByName(inviteNode.child("invitedByName").getValue(String.class));
                            invite.setFriendUid(uid);
                            Long ts = inviteNode.child("invitedAt").getValue(Long.class);
                            invite.setInvitedAt(ts != null ? ts : 0);
                            invite.setStatus("pending");
                            if (invite.getServerName() != null) {
                                result.add(invite);
                            }
                        }
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Chấp nhận / từ chối lời mời
    // -------------------------------------------------------------------------

    /**
     * Chấp nhận lời mời: tham gia server + cập nhật status = "accepted".
     */
    public void acceptServerInvite(String serverId, OnCompleteListener callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        Map<String, Object> updates = new HashMap<>();
        // Thêm user vào server_members và user_servers
        updates.put("server_members/" + serverId + "/" + uid, true);
        updates.put("user_servers/" + uid + "/" + serverId, true);
        // Cập nhật status ở cả hai path
        updates.put("server_invites/" + serverId + "/" + uid + "/status", "accepted");
        updates.put("user_invites/" + uid + "/" + serverId + "/status", "accepted");

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /**
     * Từ chối lời mời: cập nhật status = "declined".
     */
    public void declineServerInvite(String serverId, OnCompleteListener callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        Map<String, Object> updates = new HashMap<>();
        updates.put("server_invites/" + serverId + "/" + uid + "/status", "declined");
        updates.put("user_invites/" + uid + "/" + serverId + "/status", "declined");

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    public interface OnInviteListListener {
        void onSuccess(List<ServerInvite> invites);
        void onFailure(String error);
    }

    public interface OnInvitedIdsListener {
        void onLoaded(Set<String> invitedIds);
        void onFailure(String error);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
