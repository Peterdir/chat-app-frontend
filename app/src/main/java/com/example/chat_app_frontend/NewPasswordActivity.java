package com.example.chat_app_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Eye toggles
        CheckBox cbEyeNew = findViewById(R.id.cb_eye_new);
        cbEyeNew.setOnCheckedChangeListener((btn, checked) -> {
            int sel = etNewPassword.getSelectionEnd();
            etNewPassword.setTransformationMethod(checked
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            etNewPassword.setSelection(sel);
        });

        CheckBox cbEyeConfirm = findViewById(R.id.cb_eye_confirm);
        cbEyeConfirm.setOnCheckedChangeListener((btn, checked) -> {
            int sel = etConfirmPassword.getSelectionEnd();
            etConfirmPassword.setTransformationMethod(checked
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            etConfirmPassword.setSelection(sel);
        });

        findViewById(R.id.btn_reset).setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String newPass  = etNewPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

        if (newPass.length() < 6) {
            etNewPassword.setError(getString(R.string.register_error_password));
            etNewPassword.requestFocus();
            return;
        }
        if (!newPass.equals(confirm)) {
            etConfirmPassword.setError(getString(R.string.register_error_confirm));
            etConfirmPassword.requestFocus();
            return;
        }

        // TODO: call API to reset password
        Intent intent = new Intent(this, PasswordChangedActivity.class);
        // Clear the forgot-password back stack so Back doesn't return here
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
