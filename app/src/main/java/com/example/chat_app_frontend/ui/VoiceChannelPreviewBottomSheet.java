package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class VoiceChannelPreviewBottomSheet extends BottomSheetDialogFragment {

    private String channelName;
    private boolean isMuted = true; // Theo ảnh thì icon là mic off (màu đỏ)

    public VoiceChannelPreviewBottomSheet(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice_channel_preview, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvChannelName = view.findViewById(R.id.tv_preview_channel_name);
        tvChannelName.setText("\uD83D\uDD0A " + channelName);

        ImageButton btnMute = view.findViewById(R.id.btn_preview_mute);
        AppCompatButton btnJoinVoice = view.findViewById(R.id.btn_join_voice);
        ImageButton btnTextChat = view.findViewById(R.id.btn_text_chat);

        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (isMuted) {
                btnMute.setImageResource(R.drawable.ic_mic_off);
                btnMute.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                btnMute.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.white));
            } else {
                btnMute.setImageResource(R.drawable.ic_mic); // Cần chắc chắn bạn có ic_mic
                btnMute.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                btnMute.setBackgroundTintList(
                        ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray));
            }
        });

        // Nút Tham Gia Thoại xanh khổng lồ
        btnJoinVoice.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), VoiceChannelActivity.class);
            intent.putExtra("CHANNEL_NAME", channelName);
            intent.putExtra("IS_MUTED", isMuted);
            startActivity(intent);
            dismiss();
        });

        btnTextChat.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng chat text ở kênh voice", Toast.LENGTH_SHORT).show();
        });
    }
}
