package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.chat_app_frontend.R;

public class ServerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_profile);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView tvEditServerProfile = findViewById(R.id.tv_edit_server_profile);
        if (tvEditServerProfile != null) {
            tvEditServerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ServerProfileActivity.this, EditServerProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
