package com.example.chat_app_frontend.ui;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.model.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private EditText etEmail, etPassword;
    private CheckBox cbEye;
    private Button btnLogin;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance(this);

        etEmail   = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbEye      = findViewById(R.id.cb_eye);
        btnLogin  = findViewById(R.id.btn_login);

        // ProgressBar (optional – chỉ tồn tại nếu layout có khai báo)
        // progressBar = findViewById(R.id.progress_bar);

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Show / hide password toggle
        cbEye.setOnCheckedChangeListener((btn, isChecked) -> {
            int selectionEnd = etPassword.getSelectionEnd();
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(selectionEnd);
        });

        // Forgot password
        TextView tvForgot = findViewById(R.id.tv_forgot_password);
        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // Login button
        findViewById(R.id.btn_login).setOnClickListener(v -> attemptLogin());

        // Social buttons (placeholder)
        findViewById(R.id.btn_facebook).setOnClickListener(v ->
                Toast.makeText(this, "Facebook login coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_google).setOnClickListener(v ->
                Toast.makeText(this, "Google login coming soon", Toast.LENGTH_SHORT).show());

        // "Don't have an account? Register Now"
        setupRegisterLink();
    }

    // -------------------------------------------------------------------------
    // Validation & Login
    // -------------------------------------------------------------------------

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email hoặc username");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.login_error_password));
            etPassword.requestFocus();
            return;
        }

        // Show loading
        setLoading(true);

        // Gọi AuthManager để đăng nhập
        authManager.login(email, password, new AuthManager.OnAuthListener() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, 
                    "Chào mừng, " + user.getDisplayNameOrUserName() + "!", 
                    Toast.LENGTH_SHORT).show();
                
                Log.d(TAG, "✅ Đăng nhập thành công: " + user.getUserName());
                
                // Chuyển sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "❌ Đăng nhập thất bại: " + error);
            }
        });
    }

    /**
     * Hiển thị/ẩn loading indicator
     */
    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    // -------------------------------------------------------------------------
    // Spannable register link
    // -------------------------------------------------------------------------

    private void setupRegisterLink() {
        TextView tvLink = findViewById(R.id.tv_register_link);
        String full        = getString(R.string.login_register_link);        // "Don't have an account? Register Now"
        String clickPart   = getString(R.string.login_register_link_action); // "Register Now"

        SpannableString spannable = new SpannableString(full);
        int start = full.lastIndexOf(clickPart);
        if (start >= 0) {
            int end = start + clickPart.length();

            spannable.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(this, R.color.discord_blurple)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
                @Override
                public void updateDrawState(android.text.TextPaint ds) {
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
