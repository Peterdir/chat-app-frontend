package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ServerProfileBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVER_NAME = "server_name";
    private String serverName;

    public static ServerProfileBottomSheet newInstance(String serverName) {
        ServerProfileBottomSheet fragment = new ServerProfileBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_NAME, serverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverName = getArguments().getString(ARG_SERVER_NAME);
        }
        // This is to add the rounded corners and correct theme
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_server_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set dynamic server name
        TextView tvServerName = view.findViewById(R.id.tv_profile_server_name);
        if (tvServerName != null && serverName != null) {
            tvServerName.setText(serverName);
        }

        // Chỉnh sửa hồ sơ máy chủ
        View tvEditServerProfile = view.findViewById(R.id.tv_edit_server_profile);
        if (tvEditServerProfile != null) {
            tvEditServerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditServerProfileActivity.class);
                startActivity(intent);
                dismiss(); // Đóng bottom sheet sau khi mở màn hình mới
            });
        }

        // Ensure the background of the dialog window is transparent
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Configure BottomSheetBehavior
        view.post(() -> {
            View parent = (View) view.getParent();
            if (parent != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = 
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(parent);
                
                // Set peek height to approximately half the screen
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight / 2);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }
}
