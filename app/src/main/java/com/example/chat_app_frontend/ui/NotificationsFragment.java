package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.FriendSuggestionAdapter;
import com.example.chat_app_frontend.adapter.NotificationAdapter;
import com.example.chat_app_frontend.model.FriendSuggestion;
import com.example.chat_app_frontend.model.Notification;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        setupNotifications(view);
        setupFriendSuggestions(view);
        setupMoreButton(view);

        return view;
    }

    private void setupMoreButton(View view) {
        View btnMore = view.findViewById(R.id.btn_more);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_notification_settings, null);
                bottomSheetDialog.setContentView(bottomSheetView);
                
                
                if (bottomSheetDialog.getWindow() != null) {
                    View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    if (bottomSheet != null) {
                        bottomSheet.setBackgroundResource(android.R.color.transparent);
                    }
                }
                
                View btnSettings = bottomSheetView.findViewById(R.id.btn_notification_settings);
                if (btnSettings != null) {
                    btnSettings.setOnClickListener(v2 -> {
                        bottomSheetDialog.dismiss();
                        startActivity(new android.content.Intent(getContext(), NotificationSettingsActivity.class));
                    });
                }
                
                bottomSheetDialog.show();
            });
        }
    }

    private void setupNotifications(View view) {
        RecyclerView rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(
                R.drawable.avatar1,
                "Liên hệ Won của bạn đã tham gia Discord. Hãy gửi cho họ yêu cầu kết bạn!",
                "12ngày",
                "Thêm Bạn"));
        notifications.add(new Notification(
                R.drawable.avatar2,
                "mfun đã chấp nhận yêu cầu kết bạn.",
                "29ngày",
                null));

        rvNotifications.setAdapter(new NotificationAdapter(notifications));
    }

    private void setupFriendSuggestions(View view) {
        RecyclerView rvSuggestions = view.findViewById(R.id.rv_friend_suggestions);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FriendSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new FriendSuggestion(R.drawable.avatar3, "Aster", "Aster"));
        suggestions.add(new FriendSuggestion(R.drawable.avatar4, "Huỳnh Như", "Huỳnh như"));

        rvSuggestions.setAdapter(new FriendSuggestionAdapter(suggestions));
    }
}