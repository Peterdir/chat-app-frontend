package com.example.chat_app_frontend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.adapter.ServerAdapter;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.ui.DMFragment;
import com.example.chat_app_frontend.ui.NotificationsFragment;
import com.example.chat_app_frontend.ui.ProfileFragment;
import com.example.chat_app_frontend.ui.ServerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvServerRail;
    private ServerAdapter serverAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Không set padding cho root layout (v) để background tràn viền
            // Áp dụng padding trên cho rail và container để tránh bị che bởi status bar
            View rail = findViewById(R.id.rv_server_rail);
            View container = findViewById(R.id.fragment_container);
            rail.setPadding(rail.getPaddingLeft(), systemBars.top, rail.getPaddingRight(), rail.getPaddingBottom());
            container.setPadding(container.getPaddingLeft(), systemBars.top, container.getPaddingRight(), container.getPaddingBottom());
            
            // Áp dụng padding dưới cho BottomNavigationView để tránh bị che bởi gesture bar
            // Giảm bớt padding một chút để dịch chuyển icon xuống thấp hơn theo yêu cầu
            View bottomNav = findViewById(R.id.bottom_navigation);
            int safeBottomPadding = Math.max(0, systemBars.bottom - 12); // Nâng nhẹ lên 1 khoảng an toàn nhỏ
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), 
                               bottomNav.getPaddingRight(), safeBottomPadding);
            
            return insets;
        });

        setupServerRail();

        // Load DM fragment by default
        loadDMFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Hiển thị lại server rail và fragment tương ứng với lựa chọn hiện tại
                rvServerRail.setVisibility(View.VISIBLE);
                Server selectedServer = serverAdapter.getSelectedServer();
                if (selectedServer != null && !selectedServer.getId().equals("0")) {
                    loadServerFragment(selectedServer.getName());
                } else {
                    loadDMFragment();
                }
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

    private void setupServerRail() {
        rvServerRail = findViewById(R.id.rv_server_rail);
        rvServerRail.setLayoutManager(new LinearLayoutManager(this));

        List<Server> servers = new ArrayList<>();
        servers.add(new Server("0", "Direct Messages", R.drawable.ic_chat_bubble));
        servers.add(new Server("1", "CP Man", 0));
        servers.add(new Server("2", "Vietnam Gamers", 0));
        servers.add(new Server("3", "Study Group", 0));

        serverAdapter = new ServerAdapter(servers, server -> {
            serverAdapter.setSelectedServer(server.getId());
            if (server.getId().equals("0")) {
                loadDMFragment();
            } else {
                loadServerFragment(server.getName());
            }
        });

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
