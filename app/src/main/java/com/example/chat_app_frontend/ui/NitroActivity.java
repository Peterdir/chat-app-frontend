package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class NitroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_nitro);
    }
}