package com.example.chat_app_frontend.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.animation.OvershootInterpolator;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChatBottomSheet extends BottomSheetDialogFragment {

    private String channelName;

    public ChatBottomSheet(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_bottom_sheet, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Cấu hình để BottomSheet mở ra full màn hình (hoặc gần full)
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

        TextView tvWelcomeTitle = view.findViewById(R.id.tv_welcome_title);
        TextView tvWelcomeDesc = view.findViewById(R.id.tv_welcome_desc);
        TextView tvChatTitle = view.findViewById(R.id.tv_chat_title);
        ImageButton btnCloseChat = view.findViewById(R.id.btn_close_chat);
        android.widget.EditText etChatInput = view.findViewById(R.id.et_chat_input);

        String displayChannelName = (channelName != null) ? channelName : "z";

        tvWelcomeTitle.setText("Chào mừng bạn đến với " + displayChannelName + "!");
        tvWelcomeDesc.setText("Đây là sự khởi đầu của kênh " + displayChannelName + ".");
        tvChatTitle.setText("Trò chuyện");
        etChatInput.setHint("Nhắn " + displayChannelName);

        // Nút X để đóng
        if (btnCloseChat != null) {
            btnCloseChat.setOnClickListener(v -> dismiss());
        }

        ImageButton btnChatAction = view.findViewById(R.id.btn_chat_action);

        etChatInput.addTextChangedListener(new TextWatcher() {
            private boolean isShowingSend = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (btnChatAction != null) {
                    boolean hasText = s.toString().trim().length() > 0;

                    if (hasText != isShowingSend) {
                        isShowingSend = hasText;

                        // Hiệu ứng thu nhỏ xoay và mờ đi (thu gọn lại)
                        btnChatAction.animate()
                                .scaleX(0f).scaleY(0f).alpha(0f).rotation(-45f)
                                .setDuration(150) // Nhanh và mượt
                                .withEndAction(() -> {
                                    // Thay đổi icon và màu sắc khi nút đã thu nhỏ hoàn toàn
                                    if (hasText) {
                                        btnChatAction.setImageResource(android.R.drawable.ic_menu_send);
                                        btnChatAction.setBackgroundResource(R.drawable.bg_circle_dark);
                                        btnChatAction.setBackgroundTintList(
                                                ContextCompat.getColorStateList(getContext(), R.color.discord_blurple));
                                        btnChatAction
                                                .setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                                        // Set góc để khi pop lên nó xoay về 0
                                        btnChatAction.setRotation(-45f);
                                    } else {
                                        btnChatAction.setImageResource(R.drawable.ic_mic);
                                        btnChatAction.setBackgroundResource(0);
                                        btnChatAction.setBackgroundTintList(null);
                                        btnChatAction.setColorFilter(android.graphics.Color.parseColor("#B5BAC1"));
                                        // Set góc quay ngược lại để đổi gió
                                        btnChatAction.setRotation(45f);
                                    }

                                    // Hiệu ứng nảy lên (Bouncy pop up)
                                    btnChatAction.animate()
                                            .scaleX(1f).scaleY(1f).alpha(1f).rotation(0f)
                                            .setDuration(250)
                                            .setInterpolator(new OvershootInterpolator(1.5f))
                                            .start();
                                }).start();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
