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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etUsername, etDisplayName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = AuthManager.getInstance(this);

        etUsername        = findViewById(R.id.et_username);
        etDisplayName     = findViewById(R.id.et_display_name);
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister       = findViewById(R.id.btn_register);

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Show/hide password
        CheckBox cbEye = findViewById(R.id.cb_eye);
        if (cbEye != null) {
            cbEye.setOnCheckedChangeListener((btn, isChecked) -> {
                int sel = etPassword.getSelectionEnd();
                etPassword.setTransformationMethod(isChecked
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
                etPassword.setSelection(sel);
            });
        }

        // Register button
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Social buttons (placeholder)
        findViewById(R.id.btn_facebook).setOnClickListener(v ->
                Toast.makeText(this, "Facebook sign-up coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_google).setOnClickListener(v ->
                Toast.makeText(this, "Google sign-up coming soon", Toast.LENGTH_SHORT).show());

        // "Already have an account? Login Now"
        setupLoginLink();
    }

    // -------------------------------------------------------------------------
    // Validation & Register
    // -------------------------------------------------------------------------

    private void attemptRegister() {
        String username    = etUsername.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email       = etEmail.getText().toString().trim();
        String password    = etPassword.getText().toString();
        String confirm     = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError(getString(R.string.register_error_username));
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError(getString(R.string.register_error_display_name));
            etDisplayName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.register_error_email));
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError(getString(R.string.register_error_password));
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError(getString(R.string.register_error_confirm));
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        authManager.register(email, password, username, displayName, new AuthManager.OnAuthListener() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                // Hiển thị thông báo xác minh email
                Toast.makeText(RegisterActivity.this,
                        "Đăng ký thành công! Vui lòng kiểm tra email để xác minh tài khoản.",
                        Toast.LENGTH_LONG).show();

                // Quay về Login (không cho vào app ngay vì cần xác minh)
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        etUsername.setEnabled(!loading);
        etDisplayName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        btnRegister.setText(loading ? "Đang đăng ký..." : "Đăng ký");
    }

    // -------------------------------------------------------------------------
    // Spannable login link
    // -------------------------------------------------------------------------

    private void setupLoginLink() {
        TextView tvLink      = findViewById(R.id.tv_login_link);
        String full          = getString(R.string.register_login_link);
        String clickPart     = getString(R.string.register_login_link_action);

        SpannableString spannable = new SpannableString(full);
        int start = full.lastIndexOf(clickPart);
        if (start >= 0) {
            int end = start + clickPart.length();

            spannable.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(this, R.color.discord_blurple)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new ClickableSpan() {
                @Override public void onClick(View widget) {
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
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
