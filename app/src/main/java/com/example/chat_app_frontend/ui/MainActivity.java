package com.example.chat_app_frontend.ui;

import android.Manifest;
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
import com.example.chat_app_frontend.utils.FriendNotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView      rvServerRail;
    private ServerAdapter     serverAdapter;
    private FriendRepository  friendRepo;
    private ValueEventListener friendRequestListener;
    private final Set<String> knownSenderIds = new HashSet<>();
    private boolean           firstLoad      = true;

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Người dùng đã chọn Allow hoặc Deny — listener đã chạy rồi, không cần làm gì thêm
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupServerRail();

        // Tạo notification channel cho lời mời kết bạn (cần gọi trước khi hiện notification)
        FriendNotificationHelper.createNotificationChannel(this);

        // Xin quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Lắng nghe lời mời kết bạn từ bất kỳ màn hình nào
        startFriendRequestListener();

        // Load DM fragment by default
        loadDMFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Hiển thị lại server rail + DM fragment
                rvServerRail.setVisibility(View.VISIBLE);
                loadDMFragment();
                return true;
            } else if (id == R.id.nav_notifications) {
                rvServerRail.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new NotificationsFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_profile) {
                rvServerRail.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });
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
                    if (req.getSenderId() != null) knownSenderIds.add(req.getSenderId());
                }
            }
            @Override
            public void onFailure(String error) {}
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
        // ID "0" is reserved for DM/Home (Discord Logo)
        // Using 0 as iconResId will trigger the 'initial' text fallback,
        // to make it look like Discord button we might need a drawable.
        // For now let's use a placeholder or implement specific logic for the top
        // button.
        servers.add(new Server("0", "Direct Messages", R.drawable.ic_chat_bubble));
        servers.add(new Server("1", "CP Man", 0));
        servers.add(new Server("2", "Vietnam Gamers", 0));
        servers.add(new Server("3", "Study Group", 0));
        // Add more servers as needed

        serverAdapter = new ServerAdapter(servers, server -> {
            serverAdapter.setSelectedServer(server.getId());
            if (server.getId().equals("0")) {
                loadDMFragment();
            } else {
                loadServerFragment(server.getName());
            }
        });

        // Select DM by default
        serverAdapter.setSelectedServer("0");

        rvServerRail.setAdapter(serverAdapter);
    }

    private void loadDMFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new DMFragment())
                .commit();
    }

    private void loadServerFragment(String serverName) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, ServerFragment.newInstance(serverName))
                .commit();
    }
}