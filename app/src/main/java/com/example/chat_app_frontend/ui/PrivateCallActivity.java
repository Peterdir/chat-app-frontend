package com.example.chat_app_frontend.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class PrivateCallActivity extends AppCompatActivity {

    private boolean isMicMuted = false;
    private boolean isDeafened = false;
    private boolean isCameraOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_private_call);

        ImageButton btnCamera = findViewById(R.id.btn_camera);
        ImageButton btnMic = findViewById(R.id.btn_mic);
        ImageButton btnMessage = findViewById(R.id.btn_message);
        ImageButton btnDeafen = findViewById(R.id.btn_deafen);
        ImageButton btnEndCall = findViewById(R.id.btn_end_call);

        btnCamera.setOnClickListener(v -> {
            isCameraOn = !isCameraOn;
            if (isCameraOn) {
                btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btnCamera.setImageResource(R.drawable.ic_cam_on);
                Toast.makeText(this, "Đã bật Camera", Toast.LENGTH_SHORT).show();
            } else {
                btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
                btnCamera.setImageResource(R.drawable.ic_cam_off);
                Toast.makeText(this, "Đã tắt Camera", Toast.LENGTH_SHORT).show();
            }
        });

        btnMic.setOnClickListener(v -> {
            isMicMuted = !isMicMuted;
            if (isMicMuted) {
                btnMic.setImageResource(R.drawable.bg_mic_muted);
                Toast.makeText(this, "Đã tắt Micro", Toast.LENGTH_SHORT).show();
            } else {
                btnMic.setImageResource(R.drawable.ic_mic_on);
                Toast.makeText(this, "Đã bật Micro", Toast.LENGTH_SHORT).show();

                if (isDeafened) {
                    isDeafened = false;
                    btnDeafen.setImageResource(R.drawable.ic_speaker);
                    btnDeafen.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                }
            }
        });

        btnMessage.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở cửa sổ tin nhắn...", Toast.LENGTH_SHORT).show();
        });

        btnDeafen.setOnClickListener(v -> {
            isDeafened = !isDeafened;
            if (isDeafened) {
                btnDeafen.setImageResource(R.drawable.bg_deafen_muted);
                btnDeafen.setImageTintList(null);
                Toast.makeText(this, "Đã tắt âm thanh", Toast.LENGTH_SHORT).show();

                if (!isMicMuted) {
                    isMicMuted = true;
                    btnMic.setImageResource(R.drawable.bg_mic_muted);
                }
            } else {
                btnDeafen.setImageResource(R.drawable.ic_speaker);
                btnDeafen.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                Toast.makeText(this, "Đã bật âm thanh", Toast.LENGTH_SHORT).show();
            }
        });

        btnEndCall.setOnClickListener(v -> {
            Toast.makeText(this, "Đã rời khỏi cuộc gọi", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}