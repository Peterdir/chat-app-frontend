package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;

public class NotificationSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RadioButton radioAll = findViewById(R.id.radio_all_messages);
        RadioButton radioDm = findViewById(R.id.radio_dm_only);
        RadioButton radioNever = findViewById(R.id.radio_never);

        if (radioAll != null && radioDm != null && radioNever != null) {
            radioAll.setOnClickListener(v -> {
                radioAll.setChecked(true);
                radioDm.setChecked(false);
                radioNever.setChecked(false);
            });

            radioDm.setOnClickListener(v -> {
                radioAll.setChecked(false);
                radioDm.setChecked(true);
                radioNever.setChecked(false);
            });

            radioNever.setOnClickListener(v -> {
                radioAll.setChecked(false);
                radioDm.setChecked(false);
                radioNever.setChecked(true);
            });
        }
    }
}
