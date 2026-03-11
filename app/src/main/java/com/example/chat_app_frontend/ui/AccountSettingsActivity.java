package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.chat_app_frontend.R;

public class AccountSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // Nút quay lại trên Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Chuyển hướng đến màn hình Tên đăng nhập
        findViewById(R.id.rowUsername).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUsernameActivity.class);
            startActivity(intent);
        });

        // Chuyển hướng đến màn hình Tên hiển thị (Hồ sơ)
        findViewById(R.id.rowDisplayName).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Chuyển hướng đến màn hình Đổi mật khẩu
        findViewById(R.id.rowPassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}