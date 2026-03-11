package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.chat_app_frontend.R;

public class EditUsernameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_username);

        EditText etUsername = findViewById(R.id.etUsername);
        findViewById(R.id.ivClear).setOnClickListener(v -> {
            if (etUsername != null) {
                etUsername.setText("");
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
}