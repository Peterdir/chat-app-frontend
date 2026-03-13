package com.example.chat_app_frontend.ui;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendCode;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authManager = AuthManager.getInstance(this);

        etEmail     = findViewById(R.id.et_email);
        btnSendCode = findViewById(R.id.btn_send_code);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnSendCode.setOnClickListener(v -> attemptSendReset());

        setupLoginLink();
    }

    private void attemptSendReset() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.register_error_email));
            etEmail.requestFocus();
            return;
        }

        setLoading(true);

        // Firebase gửi email đặt lại mật khẩu trực tiếp — không cần OTP thủ công
        authManager.sendPasswordResetEmail(email, new AuthManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Email đặt lại mật khẩu đã được gửi!\nVui lòng kiểm tra hộp thư của bạn.",
                        Toast.LENGTH_LONG).show();
                finish(); // Quay về màn hình Login
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnSendCode.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        btnSendCode.setText(loading ? "Đang gửi..." : "Gửi email");
    }

    private void setupLoginLink() {
        TextView tvLink  = findViewById(R.id.tv_login_link);
        String full      = getString(R.string.fp_login_link);
        String clickPart = getString(R.string.fp_login_link_action);

        SpannableString spannable = new SpannableString(full);
        int start = full.lastIndexOf(clickPart);
        if (start >= 0) {
            int end = start + clickPart.length();
            spannable.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(this, R.color.discord_blurple)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ClickableSpan() {
                @Override public void onClick(View widget) { finish(); }
                @Override public void updateDrawState(android.text.TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvLink.setText(spannable);
        tvLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvLink.setHighlightColor(ContextCompat.getColor(this, android.R.color.transparent));
    }
}
