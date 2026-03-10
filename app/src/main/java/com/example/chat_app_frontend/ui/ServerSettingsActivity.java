package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.SettingsAdapter;
import com.example.chat_app_frontend.model.SettingItem;
import java.util.ArrayList;
import java.util.List;

public class ServerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_server_settings);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        List<SettingItem> data = new ArrayList<>();

        data.add(new SettingItem("Cài đặt"));
        data.add(new SettingItem("Tổng quan", R.drawable.ic_info));
        data.add(new SettingItem("Điều chỉnh", R.drawable.ic_swords));
        data.add(new SettingItem("Nhật Ký Chỉnh Sửa", R.drawable.ic_audit_log));
        data.add(new SettingItem("Kênh", R.drawable.ic_channel));
        data.add(new SettingItem("Tích hợp", R.drawable.ic_puzzle));
        data.add(new SettingItem("Emoji", R.drawable.ic_emoji));
        data.add(new SettingItem("Sticker", R.drawable.ic_emoji));
        data.add(new SettingItem("Bảo mật", R.drawable.ic_shield));

        data.add(new SettingItem("Cộng đồng"));
        data.add(new SettingItem("Kích Hoạt Cộng Đồng", R.drawable.ic_people));

        data.add(new SettingItem("Quản lý người dùng"));
        data.add(new SettingItem("Thành viên", R.drawable.ic_people));
        data.add(new SettingItem("Vai trò", R.drawable.ic_people));
        data.add(new SettingItem("Lời mời", R.drawable.ic_link));
        data.add(new SettingItem("Chặn", R.drawable.ic_hammer));

        RecyclerView recyclerView = findViewById(R.id.recycler_settings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SettingsAdapter adapter = new SettingsAdapter(data);
        recyclerView.setAdapter(adapter);
    }
}