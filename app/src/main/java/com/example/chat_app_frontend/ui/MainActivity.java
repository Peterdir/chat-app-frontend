package com.example.chat_app_frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.ServerAdapter;
import com.example.chat_app_frontend.model.FriendRequest;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.repository.ServerRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.example.chat_app_frontend.utils.FriendNotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_SERVER_ID = "open_server_id";
    public static final String EXTRA_OPEN_CHANNEL_ID = "open_channel_id";
    public static final String EXTRA_OPEN_CHANNEL_NAME = "open_channel_name";

    private RecyclerView rvServerRail;
    private View serverSidebar;
    private BottomNavigationView bottomNav;
    private ServerAdapter serverAdapter;
    private FriendRepository friendRepo;
    private ValueEventListener friendRequestListener;
    private final Set<String> knownSenderIds = new HashSet<>();
    private boolean firstLoad = true;

    private final ActivityResultLauncher<String> notifPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                // Người dùng đã chọn Allow hoặc Deny — listener đã chạy rồi, không cần làm gì
                // thêm
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Keep top/side safe area, but do not pad the root at the bottom.
            // Bottom padding here pushes BottomNavigationView too far above the gesture area.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        serverSidebar = findViewById(R.id.server_sidebar);
        setupServerRail();

        // Tạo notification channel cho lời mời kết bạn (cần gọi trước khi hiện
        // notification)
        FriendNotificationHelper.createNotificationChannel(this);

        // Xin quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Lắng nghe lời mời kết bạn từ bất kỳ màn hình nào
        startFriendRequestListener();
        syncFcmToken();

        // Load DM fragment by default
        loadDMFragment();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Hiển thị lại server rail và fragment tương ứng với lựa chọn hiện tại
                serverSidebar.setVisibility(View.VISIBLE);
                Server selectedServer = serverAdapter.getSelectedServer();
                if (selectedServer != null && !selectedServer.getId().equals("0")) {
                    loadServerFragment(selectedServer);
                } else {
                    loadDMFragment();
                }
                return true;
            } else if (id == R.id.nav_notifications) {
                serverSidebar.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new NotificationsFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_profile) {
                serverSidebar.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });

        // Nút thêm máy chủ (+)
        findViewById(R.id.btn_add_server).setOnClickListener(v -> {
            AddServerBottomSheet addServerBottomSheet = new AddServerBottomSheet();
            addServerBottomSheet.show(getSupportFragmentManager(), "AddServerBottomSheet");
        });

        maybeHandleNotificationNavigation(getIntent());
    }

    private void syncFcmToken() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null || uid.trim().isEmpty()) {
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token == null || token.trim().isEmpty()) {
                        return;
                    }
                    FirebaseManager.getDatabaseReference("user_fcm_tokens")
                            .child(uid)
                            .child(token)
                            .setValue(true);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadServerRail();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        maybeHandleNotificationNavigation(intent);
    }

    /**
     * Được gọi khi user chấp nhận lời mời server trong Notifications.
     * Reload rail ngay và chuyển về Trang chủ để thấy server mới.
     */
    public void onServerInviteAccepted(String serverId) {
        reloadServerRail(serverId, true);
    }

    private void startFriendRequestListener() {
        friendRepo = FriendRepository.getInstance();
        friendRequestListener = friendRepo.observePendingRequests(new FriendRepository.OnFriendRequestListListener() {
            @Override
            public void onLoaded(List<FriendRequest> list) {
                if (!firstLoad) {
                    for (FriendRequest req : list) {
                        String sid = req.getSenderId();
                        if (sid != null && !knownSenderIds.contains(sid)) {
                            FriendNotificationHelper.showFriendRequestNotification(
                                    MainActivity.this, req);
                        }
                    }
                }
                firstLoad = false;
                knownSenderIds.clear();
                for (FriendRequest req : list) {
                    if (req.getSenderId() != null)
                        knownSenderIds.add(req.getSenderId());
                }
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (friendRepo != null && friendRequestListener != null) {
            friendRepo.removePendingListener(friendRequestListener);
        }
    }

    private void setupServerRail() {
        rvServerRail = findViewById(R.id.rv_server_rail);
        rvServerRail.setLayoutManager(new LinearLayoutManager(this));

        List<Server> servers = new ArrayList<>();
        // ID "0" — DM / Trang chủ, giữ cố định
        servers.add(new Server("0", "Direct Messages", R.drawable.ic_chat_bubble));

        serverAdapter = new ServerAdapter(servers, server -> {
            serverAdapter.setSelectedServer(server.getId());
            if (server.getId().equals("0")) {
                loadDMFragment();
            } else {
                loadServerFragment(server);
            }
        }, server -> {
            if (!server.getId().equals("0")) {
                ServerOptionsBottomSheet bottomSheet = ServerOptionsBottomSheet
                        .newInstance(server.getId(), server.getName());
                bottomSheet.show(getSupportFragmentManager(), "ServerOptionsBottomSheet");
            }
        });

        serverAdapter.setSelectedServer("0");
        rvServerRail.setAdapter(serverAdapter);
    }

    /** Tải lại danh sách server từ Firebase, giữ lại lựa chọn hiện tại nếu có thể. */
    private void reloadServerRail() {
        reloadServerRail(null, false);
    }

    private void reloadServerRail(String preferredServerId, boolean navigateHomeAfterLoad) {
        Server selected = serverAdapter.getSelectedServer();
        String selectedId = selected != null ? selected.getId() : "0";
        String targetId = (preferredServerId != null && !preferredServerId.trim().isEmpty())
                ? preferredServerId
                : selectedId;

        serverAdapter.clearRealServers();

        ServerRepository.getInstance().getMyServers(new ServerRepository.OnServerListCallback() {
            @Override
            public void onSuccess(List<Server> servers) {
                boolean targetExists = false;
                for (Server server : servers) {
                    serverAdapter.addServer(server);
                    if (server.getId() != null && server.getId().equals(targetId)) {
                        targetExists = true;
                    }
                }

                String finalSelectedId = targetExists ? targetId : "0";
                serverAdapter.setSelectedServer(finalSelectedId);

                if (navigateHomeAfterLoad) {
                    serverSidebar.setVisibility(View.VISIBLE);
                    if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_home) {
                        bottomNav.setSelectedItemId(R.id.nav_home);
                    } else {
                        Server selectedServer = serverAdapter.getSelectedServer();
                        if (selectedServer != null && !"0".equals(selectedServer.getId())) {
                            loadServerFragment(selectedServer);
                        } else {
                            loadDMFragment();
                        }
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                // Không hiện lỗi — list vẫn show DM item
            }
        });
    }

    private void loadDMFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new DMFragment())
                .commit();
    }

    private void loadServerFragment(Server server) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, ServerFragment.newInstance(server.getId(), server.getName()))
                .commit();
    }

    private void maybeHandleNotificationNavigation(Intent intent) {
        if (intent == null) {
            return;
        }

        String serverId = intent.getStringExtra(EXTRA_OPEN_SERVER_ID);
        String channelId = intent.getStringExtra(EXTRA_OPEN_CHANNEL_ID);
        String channelName = intent.getStringExtra(EXTRA_OPEN_CHANNEL_NAME);
        String eventType = intent.getStringExtra("eventType");
        String dmSenderId = intent.getStringExtra("senderId");
        String dmSenderName = intent.getStringExtra("senderName");

        if (serverId == null || serverId.trim().isEmpty()) {
            serverId = intent.getStringExtra("serverId");
        }
        if (channelId == null || channelId.trim().isEmpty()) {
            channelId = intent.getStringExtra("channelId");
        }
        if (channelName == null || channelName.trim().isEmpty()) {
            channelName = intent.getStringExtra("channelName");
        }

        Uri data = intent.getData();
        if (data != null && "chatapp".equals(data.getScheme())) {
            if ("nitro-payment".equals(data.getHost())) {
                Intent nitroIntent = new Intent(this, NitroActivity.class);
                nitroIntent.setData(data);
                startActivity(nitroIntent);
                intent.setData(null);
                return;
            }
            if ("orbs-payment".equals(data.getHost())) {
                Intent orbsIntent = new Intent(this, BuyOrbsActivity.class);
                orbsIntent.setData(data);
                startActivity(orbsIntent);
                intent.setData(null);
                return;
            }
            if ("dm".equals(data.getHost())) {
                if (dmSenderId == null || dmSenderId.trim().isEmpty()) {
                    dmSenderId = data.getQueryParameter("friendUid");
                }
                if (dmSenderName == null || dmSenderName.trim().isEmpty()) {
                    dmSenderName = data.getQueryParameter("friendName");
                }
                eventType = "dm";
            }
            if (serverId == null || serverId.trim().isEmpty()) {
                serverId = data.getQueryParameter("serverId");
            }
            if (channelId == null || channelId.trim().isEmpty()) {
                channelId = data.getQueryParameter("channelId");
            }
            if (channelName == null || channelName.trim().isEmpty()) {
                channelName = data.getQueryParameter("channelName");
            }
        }

        if ("dm".equalsIgnoreCase(eventType) && dmSenderId != null && !dmSenderId.trim().isEmpty()) {
            Intent openDmIntent = new Intent(this, DMChatActivity.class);
            openDmIntent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, dmSenderId);
            openDmIntent.putExtra(
                    DMChatActivity.EXTRA_FRIEND_NAME,
                    (dmSenderName == null || dmSenderName.trim().isEmpty()) ? "Friend" : dmSenderName
            );
            startActivity(openDmIntent);

            intent.removeExtra("eventType");
            intent.removeExtra("senderId");
            intent.removeExtra("senderName");
            intent.removeExtra("dmId");
            intent.setData(null);
            return;
        }

        if (serverId == null || serverId.trim().isEmpty() || channelId == null || channelId.trim().isEmpty()) {
            return;
        }

        String finalChannelName = (channelName == null || channelName.trim().isEmpty()) ? "channel" : channelName;

        if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        Intent openChatIntent = new Intent(this, ServerChatActivity.class);
        openChatIntent.putExtra(ServerChatActivity.EXTRA_SERVER_ID, serverId);
        openChatIntent.putExtra(ServerChatActivity.EXTRA_CHANNEL_ID, channelId);
        openChatIntent.putExtra(ServerChatActivity.EXTRA_CHANNEL_NAME, finalChannelName);
        startActivity(openChatIntent);

        intent.removeExtra(EXTRA_OPEN_SERVER_ID);
        intent.removeExtra(EXTRA_OPEN_CHANNEL_ID);
        intent.removeExtra(EXTRA_OPEN_CHANNEL_NAME);
        intent.setData(null);
    }
}
