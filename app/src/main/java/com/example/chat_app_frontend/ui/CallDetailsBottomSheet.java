package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CallDetailsBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_call_details_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_voice_settings).setOnClickListener(v -> {
            dismiss();

            Intent intent = new Intent(getActivity(), VoiceSettingsActivity.class);
            startActivity(intent);
        });
    }
}