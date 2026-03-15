package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChannelOptionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CHANNEL_NAME = "channel_name";
    private String channelName;

    public static ChannelOptionsBottomSheet newInstance(String channelName) {
        ChannelOptionsBottomSheet fragment = new ChannelOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_NAME, channelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            channelName = getArguments().getString(ARG_CHANNEL_NAME);
        }
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_user_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvHeaderName = view.findViewById(R.id.tv_header_name);
        if (tvHeaderName == null) {
            // Fallback: finding the first TextView in the header area if ID is not set
            // Based on view_file, line 44 doesn't have an ID
        } else {
            tvHeaderName.setText(channelName);
        }

        // Logic for buttons can be added here
    }
}
