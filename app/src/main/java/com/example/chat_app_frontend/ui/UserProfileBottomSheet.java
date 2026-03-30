package com.example.chat_app_frontend.ui;

import android.content.Intent;
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

        // Logic: Bấm vào tên để hiện ra màn hình profile chi tiết
        tvName.setOnClickListener(v -> {
            if (targetObservationUid != null) {
                dismiss();
                // Giả định bạn sẽ sử dụng hoặc tạo UserProfileActivity để xem profile chi tiết
                // Nếu bạn chưa có class này, hãy tạo nó và gán layout activity_user_profile_view.xml
                try {
                    Intent intent = new Intent(requireContext(), Class.forName("com.example.chat_app_frontend.ui.UserProfileActivity"));
                    intent.putExtra("USER_ID", targetObservationUid);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(requireContext(), "Đang cập nhật tính năng hồ sơ chi tiết", Toast.LENGTH_SHORT).show();
                }
            }
        });

        View btnEditMain = view.findViewById(R.id.btn_edit_main_profile);
        String currentUid = AuthManager.getInstance(requireContext()).getUid();
        
        // Chỉ hiện nút chỉnh sửa nếu là profile của chính mình
        if (btnEditMain != null) {
            if (userId != null && !userId.equals(currentUid)) {
                btnEditMain.setVisibility(View.GONE);
            } else {
                btnEditMain.setOnClickListener(v -> {
                    dismiss();
                    Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                    startActivity(intent);
                });
            }
        }

        View btnEditServer = view.findViewById(R.id.btn_edit_server_profile);
        if (btnEditServer != null) {
            if (userId != null && !userId.equals(currentUid)) {
                btnEditServer.setVisibility(View.GONE);
            } else {
                btnEditServer.setOnClickListener(v -> {
                    dismiss();
                    Intent intent = new Intent(requireContext(), EditServerProfileActivity.class);
                    startActivity(intent);
                });
            }
        }

        View btnGetNitroCard = view.findViewById(R.id.btn_get_nitro_card);
        if (btnGetNitroCard != null && userId != null && !userId.equals(currentUid)) {
            btnGetNitroCard.setVisibility(View.GONE);
        }
    }

    private void startObservingProfile() {
        targetObservationUid = userId;
        if (targetObservationUid == null) {
            targetObservationUid = AuthManager.getInstance(requireContext()).getUid();
        }
        
        if (targetObservationUid == null) return;

        profileListener = UserRepository.getInstance().observeUser(targetObservationUid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (!isAdded()) return;

                String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUserName();
                tvName.setText(displayName);
                tvUsername.setText(user.getUserName());

                ProfileUIUtils.loadUserProfile(requireContext(), user, 
                        imgAvatar, imgDecoration, imgNamePlate, imgProfileEffect);
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
