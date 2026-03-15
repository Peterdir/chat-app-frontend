package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.chat_app_frontend.R;

public class AppearanceSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            // Không dùng setSupportActionBar để tránh bị theme can thiệp làm trắng toolbar
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.setTitle("Hiển thị");
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
