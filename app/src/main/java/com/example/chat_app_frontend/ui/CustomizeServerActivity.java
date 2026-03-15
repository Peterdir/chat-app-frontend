package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.repository.ServerRepository;

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

        Button btnCreate = findViewById(R.id.btn_create_server);
        btnCreate.setOnClickListener(v -> {
            String serverName = etServerName.getText().toString().trim();
            if (serverName.isEmpty()) {
                Toast.makeText(this, "Bạn chưa nhập tên máy chủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            btnCreate.setText("Đang tạo...");

            ServerRepository.getInstance().createServer(serverName, "", new ServerRepository.OnServerCallback() {
                @Override
                public void onSuccess(Server server) {
                    Toast.makeText(CustomizeServerActivity.this,
                            "Đã tạo máy chủ \"" + server.getName() + "\"!", Toast.LENGTH_SHORT).show();
                    // Quay về MainActivity, xóa toàn bộ stack trung gian
                    Intent intent = new Intent(CustomizeServerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Tạo");
                    Toast.makeText(CustomizeServerActivity.this,
                            "Lỗi: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
