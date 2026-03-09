package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class VoiceChannelPreviewBottomSheet extends BottomSheetDialogFragment {

    private String channelName;

    public VoiceChannelPreviewBottomSheet() {
        // Required empty public constructor
    }

    public VoiceChannelPreviewBottomSheet(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice_channel_preview, container, false);
    }

    private boolean isMuted = true;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.widget.TextView tvChannelName = view.findViewById(R.id.tv_channel_name);
        if (tvChannelName != null && channelName != null) {
            tvChannelName.setText(channelName);
        }

        android.widget.ImageButton btnMute = view.findViewById(R.id.btn_mute);
        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                isMuted = !isMuted;
                if (isMuted) {
                    btnMute.setImageResource(R.drawable.ic_mic_off);
                    btnMute.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(),
                            android.R.color.holo_red_dark));
                } else {
                    btnMute.setImageResource(R.drawable.ic_mic);
                    btnMute.setColorFilter(
                            androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.black));
                }
            });
        }

        View btnJoinVoice = view.findViewById(R.id.btn_join_voice);
        if (btnJoinVoice != null) {
            btnJoinVoice.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(),
                        VoiceChannelActivity.class);
                intent.putExtra("CHANNEL_NAME", channelName);
                intent.putExtra("IS_MUTED", isMuted);
                startActivity(intent);
            });
        }

        View btnCollapse = view.findViewById(R.id.btn_collapse);
        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(v -> dismiss());
        }
    }
}
