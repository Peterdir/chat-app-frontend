package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.ServerSettingsAdapter;
import com.example.chat_app_frontend.model.ServerSettingItem;
import java.util.ArrayList;
import java.util.List;

public class ServerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_server_settings);

        findViewById(R.id.btn_close_server_settings).setOnClickListener(v -> finish());

        List<ServerSettingItem> data = new ArrayList<>();

        data.add(new ServerSettingItem("Cài đặt"));
        data.add(new ServerSettingItem("Tổng quan", R.drawable.ic_overview));
        data.add(new ServerSettingItem("Điều chỉnh", R.drawable.ic_swords));
        data.add(new ServerSettingItem("Nhật Ký Chỉnh Sửa", R.drawable.ic_audit_log));
        data.add(new ServerSettingItem("Kênh", R.drawable.ic_channel));
        data.add(new ServerSettingItem("Tích hợp", R.drawable.ic_integrations));
        data.add(new ServerSettingItem("Emoji", R.drawable.ic_emoji));
        data.add(new ServerSettingItem("Sticker", R.drawable.ic_sticker));
        data.add(new ServerSettingItem("Bảo mật", R.drawable.ic_shield));

        data.add(new ServerSettingItem("Cộng đồng"));
        data.add(new ServerSettingItem("Kích Hoạt Cộng Đồng", R.drawable.ic_community));

        data.add(new ServerSettingItem("Quản lý người dùng"));
        data.add(new ServerSettingItem("Thành viên", R.drawable.ic_people));
        data.add(new ServerSettingItem("Vai trò", R.drawable.ic_people));
        data.add(new ServerSettingItem("Lời mời", R.drawable.ic_link));
        data.add(new ServerSettingItem("Chặn", R.drawable.ic_hammer));

        RecyclerView recyclerView = findViewById(R.id.recycler_server_settings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ServerSettingsAdapter adapter = new ServerSettingsAdapter(data);
        recyclerView.setAdapter(adapter);
    }
}