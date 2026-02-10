package com.example.chat_app_frontend;

import android.os.Bundle;

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
import com.example.chat_app_frontend.ui.ServerFragment;

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupServerRail();

        // Load DM fragment by default
        loadDMFragment();
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
                .replace(R.id.fragment_container, new DMFragment())
                .commit();
    }

    private void loadServerFragment(String serverName) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ServerFragment.newInstance(serverName))
                .commit();
    }
}