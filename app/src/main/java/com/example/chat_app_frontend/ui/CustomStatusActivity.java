package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class CustomStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_custom_status);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        EditText etStatusInput = findViewById(R.id.et_status_input);
        TextView tvStatusPreview = findViewById(R.id.tv_status_preview);

        etStatusInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    tvStatusPreview.setText("Emoji được sử dụng nhiều nhất dạo gần đây?");
                    tvStatusPreview.setTextColor(android.graphics.Color.parseColor("#B5BAC1"));
                } else {
                    tvStatusPreview.setText(s.toString());
                    tvStatusPreview.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String status = etStatusInput.getText().toString().trim();
            if (status.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập trạng thái!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã lưu trạng thái: " + status, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}