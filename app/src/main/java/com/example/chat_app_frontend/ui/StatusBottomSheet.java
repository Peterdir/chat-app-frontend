package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StatusBottomSheet extends BottomSheetDialogFragment {

    private RadioButton radioOnline, radioIdle, radioDnd, radioInvisible;

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_status_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioOnline = view.findViewById(R.id.radio_online);
        radioIdle = view.findViewById(R.id.radio_idle);
        radioDnd = view.findViewById(R.id.radio_dnd);
        radioInvisible = view.findViewById(R.id.radio_invisible);

        view.findViewById(R.id.row_online).setOnClickListener(v -> selectStatus(radioOnline, "Trực tuyến"));
        view.findViewById(R.id.row_idle).setOnClickListener(v -> selectStatus(radioIdle, "Chờ"));
        view.findViewById(R.id.row_dnd).setOnClickListener(v -> selectStatus(radioDnd, "Không làm phiền"));
        view.findViewById(R.id.row_invisible).setOnClickListener(v -> selectStatus(radioInvisible, "Vô hình"));

        view.findViewById(R.id.btn_custom_status).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomStatusActivity.class);
            startActivity(intent);
            dismiss(); // Đóng bảng trượt
        });
    }

    private void selectStatus(RadioButton selectedRadio, String statusName) {
        radioOnline.setChecked(false);
        radioIdle.setChecked(false);
        radioDnd.setChecked(false);
        radioInvisible.setChecked(false);

        selectedRadio.setChecked(true);

        Toast.makeText(getContext(), "Đã đổi thành: " + statusName, Toast.LENGTH_SHORT).show();

        dismiss();
    }
}