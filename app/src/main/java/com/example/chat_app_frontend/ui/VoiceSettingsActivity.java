package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class VoiceSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_voice_settings);

        // Nút thoát
        findViewById(R.id.btn_collapse).setOnClickListener(v -> finish());
    }
}