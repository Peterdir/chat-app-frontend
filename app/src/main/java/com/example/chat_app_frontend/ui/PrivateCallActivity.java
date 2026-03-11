package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class PrivateCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_private_call);

        findViewById(R.id.btn_collapse).setOnClickListener(v -> finish());
        findViewById(R.id.btn_end_call).setOnClickListener(v -> finish());

        TextView tvCallTitle = findViewById(R.id.tv_call_title);

        tvCallTitle.setOnClickListener(v -> {
            CallDetailsBottomSheet bottomSheet = new CallDetailsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "CallDetails");
        });
    }
}