package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class ServerMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ẩn thanh ActionBar mặc định của Android
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_server_menu);

//        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Mượn tạm nút X (btn_close) để test bảng Trạng Thái
        findViewById(R.id.btn_close).setOnClickListener(v -> {

            StatusBottomSheet bottomSheet = new StatusBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "StatusSheet");

        });

        findViewById(R.id.btn_create_own).setOnClickListener(v -> {
            startActivity(new Intent(ServerMenuActivity.this, ServerPurposeActivity.class));
        });


        int[] templateIds = {
                R.id.btn_template_gaming,
                R.id.btn_template_school,
                R.id.btn_template_study,
                R.id.btn_template_friends,
                R.id.btn_template_artists,
                R.id.btn_template_local
        };

        for (int id : templateIds) {
            findViewById(id).setOnClickListener(v -> {
                startActivity(new Intent(ServerMenuActivity.this, ServerPurposeActivity.class));
            });
        }

        findViewById(R.id.btn_join_server).setOnClickListener(v -> {
            startActivity(new Intent(ServerMenuActivity.this, JoinServerActivity.class));
        });
    }
}