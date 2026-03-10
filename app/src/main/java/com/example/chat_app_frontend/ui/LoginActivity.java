package com.example.chat_app_frontend.ui;

import com.example.chat_app_frontend.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox cbEye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail   = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbEye      = findViewById(R.id.cb_eye);

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
    // Validation
    // -------------------------------------------------------------------------

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.register_error_email));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.login_error_password));
            etPassword.requestFocus();
            return;
        }

        // TODO: call API — for now go to main
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
