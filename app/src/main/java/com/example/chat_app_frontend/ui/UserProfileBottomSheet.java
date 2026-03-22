package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.ValueEventListener;
import android.widget.ImageView;

public class UserProfileBottomSheet extends BottomSheetDialogFragment {

    private String userName;
    private String userId;
    private String targetObservationUid;
    private ValueEventListener profileListener;

    private ImageView imgBanner, imgNamePlate, imgProfileEffect, imgAvatar, imgDecoration;
    private TextView tvName, tvUsername;

    public UserProfileBottomSheet() {
        // Required empty public constructor
    }

    public UserProfileBottomSheet(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgBanner = view.findViewById(R.id.img_profile_banner);
        imgNamePlate = view.findViewById(R.id.img_profile_name_plate);
        imgProfileEffect = view.findViewById(R.id.img_profile_effect);
        imgAvatar = view.findViewById(R.id.img_profile_avatar);
        imgDecoration = view.findViewById(R.id.img_profile_decoration);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvUsername = view.findViewById(R.id.tv_profile_username);

        if (userName != null) {
            tvName.setText(userName);
        }

        startObservingProfile();

        View btnEditMain = view.findViewById(R.id.btn_edit_main_profile);
        if (btnEditMain != null) {
            btnEditMain.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            });
        }

        View btnEditServer = view.findViewById(R.id.btn_edit_server_profile);
        if (btnEditServer != null) {
            btnEditServer.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(), EditServerProfileActivity.class);
                startActivity(intent);
            });
        }

        View btnGetNitro = view.findViewById(R.id.btn_get_nitro);
        if (btnGetNitro != null) {
            btnGetNitro.setOnClickListener(v -> {
                dismiss();
                // Store/Nitro logic
                android.content.Intent intent = new android.content.Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void startObservingProfile() {
        targetObservationUid = userId;
        if (targetObservationUid == null) {
            // Fallback to current user if no ID passed
            targetObservationUid = AuthManager.getInstance(requireContext()).getUid();
        }
        
        if (targetObservationUid == null) return;

        profileListener = UserRepository.getInstance().observeUser(targetObservationUid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (!isAdded()) return;

                tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getUserName());
                tvUsername.setText(user.getUserName());

                // Sử dụng utility để load Avatar, Trang trí, Bảng tên và Hiệu ứng hồ sơ
                ProfileUIUtils.loadUserProfile(requireContext(), user, 
                        imgAvatar, imgDecoration, imgNamePlate, imgProfileEffect);
                
                // Banner (Hiện tại load mặc định hoặc từ user nếu có field)
                // if (user.getBannerUrl() != null) Glide.with(requireContext()).load(user.getBannerUrl()).into(imgBanner);
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
        if (targetObservationUid != null && profileListener != null) {
            UserRepository.getInstance().removeListener(targetObservationUid, profileListener);
        }
    }
}
