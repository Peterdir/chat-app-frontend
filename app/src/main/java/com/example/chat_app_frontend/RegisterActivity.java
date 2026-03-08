package com.example.chat_app_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etDisplayName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername        = findViewById(R.id.et_username);
        etDisplayName     = findViewById(R.id.et_display_name);
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Register button
        findViewById(R.id.btn_register).setOnClickListener(v -> attemptRegister());

        // Social buttons (placeholder)
        findViewById(R.id.btn_facebook).setOnClickListener(v ->
                Toast.makeText(this, "Facebook sign-up coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_google).setOnClickListener(v ->
                Toast.makeText(this, "Google sign-up coming soon", Toast.LENGTH_SHORT).show());

        // "Already have an account? Login Now" – make "Login Now" clickable + blurple
        setupLoginLink();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void attemptRegister() {
        String username    = etUsername.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email       = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

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

        // TODO: call API — for now go to main
        Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // -------------------------------------------------------------------------
    // Spannable login link
    // -------------------------------------------------------------------------

    private void setupLoginLink() {
        TextView tvLink = findViewById(R.id.tv_login_link);
        String full  = getString(R.string.register_login_link);     // "Already have an account? Login Now"
        String clickPart = getString(R.string.register_login_link_action); // "Login Now"

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
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
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
