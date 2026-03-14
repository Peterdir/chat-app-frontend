package com.example.chat_app_frontend.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class VoiceChannelSettingsBottomSheet extends BottomSheetDialogFragment {

    private String channelName;
    private String userName; // We might want to pass this down if available, otherwise default to "You" or
                             // "Duy"

    public VoiceChannelSettingsBottomSheet(String channelName, String userName) {
        this.channelName = channelName;
        this.userName = userName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice_channel_settings_bottom_sheet, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Kéo lên full/gần full tự động
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvChannelName = view.findViewById(R.id.tv_settings_channel_name);
        TextView tvUserName = view.findViewById(R.id.tv_settings_user_name);

        String displayChannelName = (channelName != null) ? channelName : "z";
        String displayUserName = (userName != null) ? userName : "User";

        if (tvChannelName != null) {
            tvChannelName.setText(displayChannelName);
        }
        if (tvUserName != null) {
            tvUserName.setText(displayUserName);
        }

        // Open Voice Settings Detail
        View btnVoiceSettings = view.findViewById(R.id.btn_voice_settings);
        if (btnVoiceSettings != null) {
            btnVoiceSettings.setOnClickListener(v -> {
                VoiceSettingsBottomSheet voiceSettingsSheet = new VoiceSettingsBottomSheet();
                voiceSettingsSheet.show(getParentFragmentManager(), "VoiceSettingsSheet");
            });
        }

        // Open Voice Status Detail
        View btnVoiceStatus = view.findViewById(R.id.btn_voice_status);
        if (btnVoiceStatus != null) {
            btnVoiceStatus.setOnClickListener(v -> {
                VoiceStatusBottomSheet voiceStatusSheet = new VoiceStatusBottomSheet();
                voiceStatusSheet.show(getParentFragmentManager(), "VoiceStatusSheet");
            });
        }

        // Open Invite Friends
        View btnInviteFriends = view.findViewById(R.id.btn_invite_friends);
        if (btnInviteFriends != null) {
            btnInviteFriends.setOnClickListener(v -> {
                // Đóng sheet hiện tại (VoiceChannelSettings)
                dismiss();
                // Mở sheet Invite Friends
                InviteFriendsBottomSheet inviteSheet = new InviteFriendsBottomSheet(channelName);
                inviteSheet.show(requireActivity().getSupportFragmentManager(), "InviteSheet");
            });
        }

        // Open User Profile
        View btnUserProfile = view.findViewById(R.id.btn_user_profile);
        if (btnUserProfile != null) {
            btnUserProfile.setOnClickListener(v -> {
                dismiss();
                UserProfileBottomSheet profileSheet = new UserProfileBottomSheet(displayUserName);
                profileSheet.show(requireActivity().getSupportFragmentManager(), "UserProfileSheet");
            });
        }

        // Open Channel Settings
        View btnChannelSettings = view.findViewById(R.id.btn_channel_settings);
        if (btnChannelSettings != null) {
            btnChannelSettings.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(),
                        ChannelSettingsActivity.class);
                startActivity(intent);
            });
        }

        // Logic for switches could go here
        SwitchCompat switchMute = view.findViewById(R.id.switch_mute);
        SwitchCompat switchVideoOnly = view.findViewById(R.id.switch_video_only);

        if (switchMute != null) {
            switchMute.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Xử lý mute all
            });
        }

        if (switchVideoOnly != null) {
            switchVideoOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Xử lý chỉ xem video
            });
        }
    }
}
