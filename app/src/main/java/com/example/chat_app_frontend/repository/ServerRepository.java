package com.example.chat_app_frontend.repository;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository xử lý tất cả thao tác Firebase cho Server.
 *
 * Firebase schema:
 *   servers/{serverId}/         — metadata của server
 *   server_members/{serverId}/{uid} — thành viên của server
 *   user_servers/{uid}/{serverId}   — server mà user tham gia
 *   server_channels/{serverId}/{channelId} — kênh của server
 */
public class ServerRepository {

    private static ServerRepository instance;

    public static ServerRepository getInstance() {
        if (instance == null) instance = new ServerRepository();
        return instance;
    }

    private ServerRepository() {}

    // -------------------------------------------------------------------------
    // Callbacks
    // -------------------------------------------------------------------------

    public interface OnServerListCallback {
        void onSuccess(List<Server> servers);
        void onFailure(String error);
    }

    public interface OnServerCallback {
        void onSuccess(Server server);
        void onFailure(String error);
    }

    public interface OnChannelListCallback {
        void onSuccess(List<Channel> channels);
        void onFailure(String error);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Tạo server mới trên Firebase với các channel mặc định.
     * Viết vào: servers/, user_servers/, server_members/, server_channels/
     */
    public void createServer(String serverName, String iconUrl, OnServerCallback callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        DatabaseReference serversRef = FirebaseManager.getServersRef();
        String serverId = serversRef.push().getKey();
        if (serverId == null) { callback.onFailure("Không thể tạo server ID"); return; }

        String inviteCode = generateInviteCode();
        Server server = new Server(serverId, serverName, iconUrl != null ? iconUrl : "", uid, inviteCode);
        server.setCreatedAt(System.currentTimeMillis());

        serversRef.child(serverId).setValue(server.toMap())
                .addOnSuccessListener(unused -> createDefaultChannels(serverId, () -> {
                    DatabaseReference userServersRef = FirebaseManager.getDatabaseReference("user_servers/" + uid + "/" + serverId);
                    DatabaseReference memberRef = FirebaseManager.getDatabaseReference("server_members/" + serverId + "/" + uid);
                    userServersRef.setValue(true);
                    memberRef.setValue(true)
                            .addOnSuccessListener(u -> callback.onSuccess(server))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                }))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lấy danh sách server mà current user đã tham gia.
     * Đọc từ user_servers/{uid}/ → tải metadata từng server.
     */
    public void getMyServers(OnServerListCallback callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        FirebaseManager.getDatabaseReference("user_servers/" + uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            callback.onSuccess(new ArrayList<>());
                            return;
                        }

                        List<String> serverIds = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey() != null) serverIds.add(child.getKey());
                        }

                        List<Server> servers = new ArrayList<>();
                        AtomicInteger loaded = new AtomicInteger(0);
                        int total = serverIds.size();

                        for (String serverId : serverIds) {
                            FirebaseManager.getServersRef().child(serverId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            Server server = serverFromSnapshot(snap);
                                            if (server != null) servers.add(server);
                                            if (loaded.incrementAndGet() == total) callback.onSuccess(servers);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError e) {
                                            if (loaded.incrementAndGet() == total) callback.onSuccess(servers);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Lấy danh sách channel của server, sắp xếp theo position.
     */
    public void getServerChannels(String serverId, OnChannelListCallback callback) {
        FirebaseManager.getDatabaseReference("server_channels/" + serverId)
                .orderByChild("position")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Channel> channels = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Channel channel = channelFromSnapshot(child);
                            if (channel != null) channels.add(channel);
                        }
                        callback.onSuccess(channels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Lắng nghe realtime cập nhật channel của server.
     * Trả về ValueEventListener để caller có thể hủy khi không cần nữa.
     */
    public ValueEventListener observeServerChannels(String serverId, OnChannelListCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Channel> channels = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Channel channel = channelFromSnapshot(child);
                    if (channel != null) channels.add(channel);
                }
                callback.onSuccess(channels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };
        FirebaseManager.getDatabaseReference("server_channels/" + serverId)
                .orderByChild("position")
                .addValueEventListener(listener);
        return listener;
    }

    public void removeChannelListener(String serverId, ValueEventListener listener) {
        FirebaseManager.getDatabaseReference("server_channels/" + serverId)
                .orderByChild("position")
                .removeEventListener(listener);
    }

    /**
     * Tham gia server bằng invite code.
     */
    public void joinServerByInviteCode(String inviteCode, OnServerCallback callback) {
        String uid = currentUid();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        FirebaseManager.getServersRef()
                .orderByChild("inviteCode")
                .equalTo(inviteCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            callback.onFailure("Không tìm thấy server với mã mời này");
                            return;
                        }

                        DataSnapshot serverSnap = snapshot.getChildren().iterator().next();
                        String serverId = serverSnap.getKey();
                        Server server = serverFromSnapshot(serverSnap);

                        if (serverId == null || server == null) {
                            callback.onFailure("Dữ liệu server không hợp lệ");
                            return;
                        }

                        // Check already member
                        FirebaseManager.getDatabaseReference("server_members/" + serverId + "/" + uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot memberSnap) {
                                        if (memberSnap.exists()) {
                                            // Already a member — still return success with server info
                                            callback.onSuccess(server);
                                            return;
                                        }
                                        // Add membership
                                        FirebaseManager.getDatabaseReference("user_servers/" + uid + "/" + serverId).setValue(true);
                                        FirebaseManager.getDatabaseReference("server_members/" + serverId + "/" + uid)
                                                .setValue(true)
                                                .addOnSuccessListener(u -> callback.onSuccess(server))
                                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError e) {
                                        callback.onFailure(e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void createDefaultChannels(String serverId, Runnable onDone) {
        DatabaseReference channelsRef = FirebaseManager.getDatabaseReference("server_channels/" + serverId);

        String[] names = {"welcome-and-rules", "general", "voice-chat"};
        String[] types = {"text", "text", "voice"};

        AtomicInteger done = new AtomicInteger(0);
        for (int i = 0; i < names.length; i++) {
            String channelId = channelsRef.push().getKey();
            Channel channel = new Channel(channelId, names[i], types[i]);
            channel.setPosition(i);
            channelsRef.child(channelId).setValue(channel.toMap())
                    .addOnCompleteListener(t -> {
                        if (done.incrementAndGet() == names.length) onDone.run();
                    });
        }
    }

    private Server serverFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String id = snapshot.getKey();
        String name = snapshot.child("name").getValue(String.class);
        if (name == null) return null;
        String iconUrl = snapshot.child("iconUrl").getValue(String.class);
        String ownerId = snapshot.child("ownerId").getValue(String.class);
        String inviteCode = snapshot.child("inviteCode").getValue(String.class);
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);

        Server server = new Server(id, name, iconUrl != null ? iconUrl : "", ownerId, inviteCode);
        if (createdAt != null) server.setCreatedAt(createdAt);
        return server;
    }

    private Channel channelFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String id = snapshot.getKey();
        String name = snapshot.child("name").getValue(String.class);
        String type = snapshot.child("type").getValue(String.class);
        if (name == null || type == null) return null;
        Long pos = snapshot.child("position").getValue(Long.class);

        Channel channel = new Channel(id, name, type);
        if (pos != null) channel.setPosition(pos.intValue());
        return channel;
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String generateInviteCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
