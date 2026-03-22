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
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.ValueEventListener;
import android.widget.ImageView;

public class VoiceChannelSettingsBottomSheet extends BottomSheetDialogFragment {

    private String channelName;
    private String userName; // We might want to pass this down if available, otherwise default to "You" or
                             // "Duy"
    
    private ValueEventListener userProfileListener;
    private ImageView imgMemberAvatar, imgMemberDecoration, imgMemberNamePlate;
    private TextView tvMemberName, tvSettingsUserName;

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
                InviteFriendsBottomSheet inviteSheet = InviteFriendsBottomSheet
                        .newInstanceForChannel(channelName);
                inviteSheet.show(requireActivity().getSupportFragmentManager(), "InviteSheet");
            });
        }

        // Open User Profile
        View btnUserProfile = view.findViewById(R.id.btn_user_profile);
        if (btnUserProfile != null) {
            btnUserProfile.setOnClickListener(v -> {
                dismiss();
                String uid = AuthManager.getInstance(requireContext()).getUid();
                UserProfileBottomSheet profileSheet = new UserProfileBottomSheet(displayUserName, uid);
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

        // Bắt đầu quan sát hồ sơ để đồng bộ thời gian thực
        startObservingUserProfile(view);
    }

    private void startObservingUserProfile(View view) {
        tvSettingsUserName = view.findViewById(R.id.tv_settings_user_name);
        tvMemberName = view.findViewById(R.id.tv_member_name);
        imgMemberAvatar = view.findViewById(R.id.img_member_avatar);
        imgMemberDecoration = view.findViewById(R.id.img_member_decoration);
        imgMemberNamePlate = view.findViewById(R.id.img_member_name_plate);

        String uid = AuthManager.getInstance(requireContext()).getUid();
        if (uid == null) return;

        userProfileListener = UserRepository.getInstance().observeUser(uid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (!isAdded()) return;

                // Cập nhật tên ở các vị trí
                if (user.getDisplayName() != null) {
                    if (tvSettingsUserName != null) tvSettingsUserName.setText(user.getDisplayName());
                    if (tvMemberName != null) tvMemberName.setText(user.getDisplayName());
                }

                // Cập nhật Avatar, Trang trí và Bảng tên hàng thành viên
                ProfileUIUtils.loadUserProfile(requireContext(), user, 
                        imgMemberAvatar, imgMemberDecoration, imgMemberNamePlate, null);
            }

            @Override
            public void onUserNotFound() {}

            @Override
            public void onFailure(String error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        String uid = AuthManager.getInstance(requireContext()).getUid();
        if (uid != null && userProfileListener != null) {
            UserRepository.getInstance().removeListener(uid, userProfileListener);
        }
    }
}
