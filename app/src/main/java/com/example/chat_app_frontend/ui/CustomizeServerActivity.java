package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class CustomizeServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_customize_server);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvDisclaimer = findViewById(R.id.tv_disclaimer);
        String text = "Khi tạo máy chủ, nghĩa là bạn đã đồng ý với <font color='#00A8FC'>Nguyên Tắc Cộng Đồng</font> của Discord.";
        tvDisclaimer.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));

        EditText etServerName = findViewById(R.id.et_server_name);
        findViewById(R.id.btn_clear_text).setOnClickListener(v -> {
            etServerName.setText("");
            etServerName.requestFocus();
        });

        findViewById(R.id.btn_upload_image).setOnClickListener(v -> {
            Toast.makeText(this, "Mở thư viện ảnh (Sẽ code sau)", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_create_server).setOnClickListener(v -> {
            String serverName = etServerName.getText().toString().trim();
            if(serverName.isEmpty()){
                Toast.makeText(this, "Bạn chưa nhập tên máy chủ!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đang tạo máy chủ: " + serverName, Toast.LENGTH_SHORT).show();
            }
        });
    }
}