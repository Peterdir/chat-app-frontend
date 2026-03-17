package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.chat_app_frontend.R;

public class ServerOptionsBottomSheet extends DialogFragment {

    private static final String ARG_SERVER_ID = "server_id";
    private static final String ARG_SERVER_NAME = "server_name";
    private String serverId;
    private String serverName;

    public static ServerOptionsBottomSheet newInstance(String serverId, String serverName) {
        ServerOptionsBottomSheet fragment = new ServerOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        args.putString(ARG_SERVER_NAME, serverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverId = getArguments().getString(ARG_SERVER_ID, "");
            serverName = getArguments().getString(ARG_SERVER_NAME);
        }
        setStyle(STYLE_NORMAL, R.style.FloatingServerMenuTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_server_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvServerName = view.findViewById(R.id.tv_server_name);
        if (tvServerName != null) {
            tvServerName.setText(serverName);
        }

        // Setup click listeners
        view.findViewById(R.id.option_mark_as_read).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.option_notifications).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.option_invite_friends).setOnClickListener(v -> {
            dismiss();
            InviteFriendsBottomSheet inviteSheet = InviteFriendsBottomSheet
                    .newInstanceForServer(serverId, serverName);
            inviteSheet.show(getParentFragmentManager(), "InviteFriendsFromServerOptions");
        });

        view.findViewById(R.id.option_more_settings).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ServerSettingsActivity.class);
            startActivity(intent);
            dismiss();
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            
            // Position it floating near the sidebar like in the screenshot
            params.gravity = Gravity.TOP | Gravity.START;
            
            // X offset: slightly more than sidebar width (72dp)
            params.x = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, getResources().getDisplayMetrics());
            
            // Y offset: centered-ish top area
            params.y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
            
            window.setAttributes(params);
            
            // Make the dialog background transparent (the container layout has the background)
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
