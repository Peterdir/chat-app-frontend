package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu trạng thái!", Toast.LENGTH_SHORT).show();
            finish();
        });

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

        CardView btnDeleteAt = findViewById(R.id.btn_delete_at);
        TextView tvDeleteAtTime = findViewById(R.id.tv_delete_at_time);

        btnDeleteAt.setOnClickListener(v -> {
            DeleteAtBottomSheet bottomSheet = new DeleteAtBottomSheet();

            bottomSheet.setOnTimeSelectedListener(time -> {
                tvDeleteAtTime.setText(time);
            });

            bottomSheet.show(getSupportFragmentManager(), "DeleteAtSheet");
        });
    }
}