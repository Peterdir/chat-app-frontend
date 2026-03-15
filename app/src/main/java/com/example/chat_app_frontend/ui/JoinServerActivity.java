package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.repository.ServerRepository;

public class JoinServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_join_server);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvHint = findViewById(R.id.tv_link_hint);
        String hintText = "Lời mời trông giống <font color='#FFFFFF'>https://discord.gg/hTKzmak</font>, <font color='#FFFFFF'>hTKzmak</font> hoặc <font color='#FFFFFF'>https://discord.gg/wumpus-friends</font>";
        tvHint.setText(Html.fromHtml(hintText, Html.FROM_HTML_MODE_LEGACY));

        EditText etLink = findViewById(R.id.et_invite_link);
        Button btnJoin = findViewById(R.id.btn_join_by_link);

        btnJoin.setOnClickListener(v -> {
            String input = etLink.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập liên kết hoặc mã mời!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Trích xuất invite code từ URL hoặc dùng trực tiếp
            String inviteCode = extractCode(input);

            btnJoin.setEnabled(false);
            btnJoin.setText("Đang tham gia...");

            ServerRepository.getInstance().joinServerByInviteCode(inviteCode, new ServerRepository.OnServerCallback() {
                @Override
                public void onSuccess(Server server) {
                    Toast.makeText(JoinServerActivity.this,
                            "Đã tham gia \"" + server.getName() + "\"!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(JoinServerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    btnJoin.setEnabled(true);
                    btnJoin.setText("Tham Gia Máy Chủ");
                    Toast.makeText(JoinServerActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });

        findViewById(R.id.btn_join_student_hub).setOnClickListener(v -> {
            startActivity(new Intent(JoinServerActivity.this, StudentHubActivity.class));
        });
    }

    /** Trích xuất invite code từ URL (ví dụ: https://discord.gg/abc123 → abc123). */
    private String extractCode(String input) {
        // Hỗ trợ định dạng: https://discord.gg/CODE hoặc CODE thuần
        if (input.contains("/")) {
            String[] parts = input.split("/");
            return parts[parts.length - 1].trim();
        }
        return input.trim();
    }
}
