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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserProfileBottomSheet extends BottomSheetDialogFragment {

    private String userName;

    public UserProfileBottomSheet() {
        // Required empty public constructor
    }

    public UserProfileBottomSheet(String userName) {
        this.userName = userName;
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

        TextView tvName = view.findViewById(R.id.tv_profile_name);
        if (tvName != null && userName != null && !userName.isEmpty()) {
            tvName.setText(userName);
        }

        View btnEditMain = view.findViewById(R.id.btn_edit_main_profile);
        if (btnEditMain != null) {
            btnEditMain.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            });
        }

        View btnGetNitro = view.findViewById(R.id.btn_get_nitro);
        if (btnGetNitro != null) {
            btnGetNitro.setOnClickListener(v -> {
                dismiss();
                android.content.Intent intent = new android.content.Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
