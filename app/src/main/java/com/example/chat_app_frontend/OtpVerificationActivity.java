package com.example.chat_app_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
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

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        etOtp1 = findViewById(R.id.et_otp1);
        etOtp2 = findViewById(R.id.et_otp2);
        etOtp3 = findViewById(R.id.et_otp3);
        etOtp4 = findViewById(R.id.et_otp4);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Auto-advance and auto-back between OTP boxes
        setupOtpNavigation();

        findViewById(R.id.btn_verify).setOnClickListener(v -> attemptVerify());

        setupResendLink();
    }

    // -------------------------------------------------------------------------
    // Auto-advance between OTP boxes
    // -------------------------------------------------------------------------

    private void setupOtpNavigation() {
        autoAdvance(etOtp1, null,   etOtp2);
        autoAdvance(etOtp2, etOtp1, etOtp3);
        autoAdvance(etOtp3, etOtp2, etOtp4);
        autoAdvance(etOtp4, etOtp3, null);
    }

    private void autoAdvance(EditText current, EditText prev, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                } else if (s.length() == 0 && prev != null) {
                    prev.requestFocus();
                }
                updateBoxBackground(current, s.length() > 0);
            }
        });
    }

    private void updateBoxBackground(EditText box, boolean hasFill) {
        box.setBackground(ContextCompat.getDrawable(this,
                hasFill ? R.drawable.bg_otp_box : R.drawable.bg_otp_box_empty));
    }

    // -------------------------------------------------------------------------
    // Verify
    // -------------------------------------------------------------------------

    private void attemptVerify() {
        String otp = etOtp1.getText().toString()
                + etOtp2.getText().toString()
                + etOtp3.getText().toString()
                + etOtp4.getText().toString();

        if (otp.length() < 4) {
            Toast.makeText(this, getString(R.string.otp_error_incomplete), Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: validate OTP against API
        startActivity(new Intent(this, NewPasswordActivity.class));
    }

    // -------------------------------------------------------------------------
    // Resend link
    // -------------------------------------------------------------------------

    private void setupResendLink() {
        TextView tvResend = findViewById(R.id.tv_resend);
        String full      = getString(R.string.otp_resend_link);
        String clickPart = getString(R.string.otp_resend_link_action);

        SpannableString spannable = new SpannableString(full);
        int start = full.lastIndexOf(clickPart);
        if (start >= 0) {
            int end = start + clickPart.length();
            spannable.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(this, R.color.discord_blurple)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ClickableSpan() {
                @Override public void onClick(View widget) {
                    // TODO: call API to resend OTP
                    Toast.makeText(OtpVerificationActivity.this,
                            getString(R.string.otp_resent), Toast.LENGTH_SHORT).show();
                }
                @Override public void updateDrawState(android.text.TextPaint ds) {
                    super.updateDrawState(ds); ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvResend.setText(spannable);
        tvResend.setMovementMethod(LinkMovementMethod.getInstance());
        tvResend.setHighlightColor(ContextCompat.getColor(this, android.R.color.transparent));
    }
}
