package com.example.chat_app_frontend.ui;

import com.example.chat_app_frontend.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordChangedActivity extends AppCompatActivity {

    private static final long AUTO_REDIRECT_MS = 2500L;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_changed);

        // Tap "Back to Login" immediately
        findViewById(R.id.btn_back_to_login).setOnClickListener(v -> goToLogin());

        // Auto-redirect to login after 2.5 seconds
        handler.postDelayed(this::goToLogin, AUTO_REDIRECT_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
