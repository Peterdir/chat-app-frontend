package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

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
        findViewById(R.id.btn_join_by_link).setOnClickListener(v -> {
            String link = etLink.getText().toString().trim();
            if(link.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập liên kết mời!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đang kiểm tra link: " + link, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_join_student_hub).setOnClickListener(v -> {
            startActivity(new Intent(JoinServerActivity.this, StudentHubActivity.class));
        });
    }
}