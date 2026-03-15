package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class ServerPurposeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_server_purpose);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvSkip = findViewById(R.id.tv_skip);
        String text = "Bạn không chắc? Bạn có thể tạm thời <font color='#00A8FC'>bỏ qua câu hỏi này.</font>";
        tvSkip.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));

        findViewById(R.id.btn_for_friends).setOnClickListener(v -> openNextScreen());
        findViewById(R.id.btn_for_community).setOnClickListener(v -> openNextScreen());
        tvSkip.setOnClickListener(v -> openNextScreen());
    }

    private void openNextScreen() {
        Intent intent = new Intent(ServerPurposeActivity.this, CustomizeServerActivity.class);
        startActivity(intent);
    }
}