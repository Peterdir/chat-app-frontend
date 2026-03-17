package com.example.chat_app_frontend.repository;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.Category;
import com.example.chat_app_frontend.model.CategoryWithChannels;
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

    public interface OnCategoryCallback {
        void onSuccess(Category category);
        void onFailure(String error);
    }

    public interface OnCategoryListCallback {
        void onSuccess(List<Category> categories);
        void onFailure(String error);
    }

    public interface OnCategoriesWithChannelsCallback {
        void onSuccess(List<CategoryWithChannels> categoriesWithChannels, List<Channel> uncategorizedChannels);
        void onFailure(String error);
    }

    public interface OnCompleteCallback {
        void onComplete();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Tạo server mới trên Firebase.
     * Viết vào: servers/, user_servers/, server_members/
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
                .addOnSuccessListener(unused -> {
                    DatabaseReference userServersRef = FirebaseManager.getDatabaseReference("user_servers/" + uid + "/" + serverId);
                    DatabaseReference memberRef = FirebaseManager.getDatabaseReference("server_members/" + serverId + "/" + uid);
                    userServersRef.setValue(true);
                    memberRef.setValue(true)
                            .addOnSuccessListener(u -> callback.onSuccess(server))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Dọn các kênh mock mặc định cũ để chỉ giữ dữ liệu thật do user tạo.
     */
    public void removeLegacyDefaultChannels(String serverId, OnCompleteCallback callback) {
        FirebaseManager.getDatabaseReference("server_channels/" + serverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onComplete();
                            return;
                        }

                        List<DataSnapshot> toDelete = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String name = child.child("name").getValue(String.class);
                            String type = child.child("type").getValue(String.class);
                            String categoryId = child.child("categoryId").getValue(String.class);

                            boolean isLegacyText = ("welcome-and-rules".equals(name) || "general".equals(name))
                                    && "text".equals(type);
                            boolean isLegacyVoice = "voice-chat".equals(name) && "voice".equals(type);
                            boolean isUncategorized = categoryId == null || categoryId.isEmpty();

                            if ((isLegacyText || isLegacyVoice) && isUncategorized) {
                                toDelete.add(child);
                            }
                        }

                        if (toDelete.isEmpty()) {
                            callback.onComplete();
                            return;
                        }

                        AtomicInteger done = new AtomicInteger(0);
                        int total = toDelete.size();
                        for (DataSnapshot item : toDelete) {
                            item.getRef().removeValue().addOnCompleteListener(task -> {
                                if (done.incrementAndGet() == total) {
                                    callback.onComplete();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onComplete();
                    }
                });
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
     * Tạo category mới trong server.
     */
    public void createCategory(String serverId, String categoryName, OnCategoryCallback callback) {
        if (serverId == null || categoryName == null) {
            callback.onFailure("Dữ liệu không hợp lệ");
            return;
        }

        DatabaseReference categoriesRef = FirebaseManager.getDatabaseReference("server_categories/" + serverId);
        String categoryId = categoriesRef.push().getKey();
        if (categoryId == null) {
            callback.onFailure("Không thể tạo category ID");
            return;
        }

        Category category = new Category(categoryId, categoryName);
        category.setPosition((int) (System.nanoTime() % Integer.MAX_VALUE)); // Use nanotime for ordering
        categoriesRef.child(categoryId).setValue(category.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess(category))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lấy danh sách category của server.
     */
    public void getServerCategories(String serverId, OnCategoryListCallback callback) {
        FirebaseManager.getDatabaseReference("server_categories/" + serverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Category> categories = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Category category = categoryFromSnapshot(child);
                            if (category != null) categories.add(category);
                        }
                        callback.onSuccess(categories);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Tạo channel mới trong server, có thể thuộc một category hoặc không.
     */
    public void createChannel(String serverId, String channelName, String channelType, 
                             String categoryId, OnChannelListCallback callback) {
        if (serverId == null || channelName == null || channelType == null) {
            callback.onFailure("Dữ liệu không hợp lệ");
            return;
        }

        DatabaseReference channelsRef = FirebaseManager.getDatabaseReference("server_channels/" + serverId);
        String channelId = channelsRef.push().getKey();
        if (channelId == null) {
            callback.onFailure("Không thể tạo channel ID");
            return;
        }

        Channel channel = new Channel(channelId, channelName, channelType, categoryId);
        channel.setPosition((int) System.nanoTime());
        channelsRef.child(channelId).setValue(channel.toMap())
                .addOnSuccessListener(unused -> {
                    // Lấy danh sách channel cập nhật
                    getServerChannels(serverId, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lấy danh sách category với channels được nhóm theo category.
     */
    public void getServerCategoriesWithChannels(String serverId, OnCategoriesWithChannelsCallback callback) {
        getServerCategories(serverId, new OnCategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                getServerChannels(serverId, new OnChannelListCallback() {
                    @Override
                    public void onSuccess(List<Channel> channels) {
                        // Group channels by category
                        List<CategoryWithChannels> categoriesWithChannels = new ArrayList<>();
                        List<Channel> uncategorizedChannels = new ArrayList<>();

                        for (Category category : categories) {
                            CategoryWithChannels cwc = new CategoryWithChannels(category);
                            for (Channel channel : channels) {
                                if (category.getId().equals(channel.getCategoryId())) {
                                    cwc.addChannel(channel);
                                }
                            }
                            categoriesWithChannels.add(cwc);
                        }

                        // Collect uncategorized channels
                        for (Channel channel : channels) {
                            if (channel.getCategoryId() == null) {
                                uncategorizedChannels.add(channel);
                            }
                        }

                        callback.onSuccess(categoriesWithChannels, uncategorizedChannels);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure("Failed to load channels: " + error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure("Failed to load categories: " + error);
            }
        });
    }
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
        String categoryId = snapshot.child("categoryId").getValue(String.class);

        Channel channel = new Channel(id, name, type, categoryId);
        if (pos != null) channel.setPosition(pos.intValue());
        return channel;
    }

    private Category categoryFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String id = snapshot.getKey();
        String name = snapshot.child("name").getValue(String.class);
        if (name == null) return null;
        Long pos = snapshot.child("position").getValue(Long.class);

        Category category = new Category(id, name);
        if (pos != null) category.setPosition(pos.intValue());
        return category;
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
