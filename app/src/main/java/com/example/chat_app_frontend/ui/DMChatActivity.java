package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.MessageAdapter;
import com.example.chat_app_frontend.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DMChatActivity extends AppCompatActivity {

    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_STATUS = "friend_status";
    public static final String EXTRA_FRIEND_AVATAR = "friend_avatar";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private String friendName;

    // Current user mock data
    private static final String SELF_ID = "me";
    private static final String SELF_NAME = "HoangGJinn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_chat);

        friendName = getIntent().getStringExtra(EXTRA_FRIEND_NAME);
        String friendStatus = getIntent().getStringExtra(EXTRA_FRIEND_STATUS);
        int friendAvatarRes = getIntent().getIntExtra(EXTRA_FRIEND_AVATAR, 0);
        if (friendName == null) friendName = "Friend";
        if (friendStatus == null) friendStatus = "Online";

        // Toolbar
        setupToolbar(friendName, friendStatus, friendAvatarRes);

        // Input
        etMessageInput = findViewById(R.id.et_message_input);
        etMessageInput.setHint("Nhắn tin với @" + friendName);

        ImageView btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        // RecyclerView
        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageList = buildMockDMMessages(friendName, friendAvatarRes);
        messageAdapter = new MessageAdapter(messageList);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.scrollToPosition(messageList.size() - 1);

        // Call buttons
        ImageView btnCall = findViewById(R.id.btn_call);
        ImageView btnVideoCall = findViewById(R.id.btn_video_call);

        btnCall.setOnClickListener(v -> startPrivateCall(false));
        btnVideoCall.setOnClickListener(v -> startPrivateCall(true));
    }

    private void startPrivateCall(boolean isVideo) {
        android.content.Intent intent = new android.content.Intent(this, PrivateCallActivity.class);
        intent.putExtra(EXTRA_FRIEND_NAME, friendName);
        intent.putExtra("is_video", isVideo);
        startActivity(intent);
    }

    private void setupToolbar(String name, String status, int avatarRes) {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        TextView tvFriendName = findViewById(R.id.tv_friend_name);
        tvFriendName.setText(name);

        TextView tvFriendStatus = findViewById(R.id.tv_friend_status);
        tvFriendStatus.setText(status);
        // Color status text
        switch (status.toLowerCase()) {
            case "online":
                tvFriendStatus.setTextColor(getColor(R.color.discord_green));
                break;
            case "idle":
                tvFriendStatus.setTextColor(0xFFfaa61a);
                break;
            case "dnd":
                tvFriendStatus.setTextColor(0xFFed4245);
                break;
            default:
                tvFriendStatus.setTextColor(getColor(R.color.discord_text_secondary));
        }

        ImageView imgAvatar = findViewById(R.id.img_friend_avatar);
        TextView tvInitial = findViewById(R.id.tv_friend_initial);
        if (avatarRes != 0) {
            imgAvatar.setImageResource(avatarRes);
            imgAvatar.setVisibility(View.VISIBLE);
            tvInitial.setVisibility(View.GONE);
        } else {
            imgAvatar.setVisibility(View.GONE);
            tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
            tvInitial.setVisibility(View.VISIBLE);
        }
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        boolean isFirst = true;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message last = messageList.get(i);
            if (!Message.TYPE_DATE_DIVIDER.equals(last.getMessageType())) {
                isFirst = !SELF_ID.equals(last.getSenderId());
                break;
            }
        }

        Message msg = new Message(
                String.valueOf(System.currentTimeMillis()),
                SELF_ID, SELF_NAME, 0, text, "Hôm nay lúc " + time,
                true, isFirst
        );
        messageList.add(msg);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etMessageInput.setText("");
    }

    /**
     * Builds sample DM messages inspired by the Discord DM screenshots.
     */
    private List<Message> buildMockDMMessages(String friendName, int friendAvatarRes) {
        String friendId = "friend";
        List<Message> list = new ArrayList<>();

        // Date divider
        list.add(new Message("3 tháng 1 năm 2026"));

        // Friend message
        list.add(new Message("1", friendId, friendName, friendAvatarRes,
                "Có gì zui zậy Giáp 😮", "03/01/2026 17:07", false, true));

        // Date divider
        list.add(new Message("6 tháng 1 năm 2026"));

        // Self message group
        list.add(new Message("2", SELF_ID, SELF_NAME, 0,
                "bij hack Thuy oi", "06/01/2026 01:32", true, true));
        list.add(new Message("3", SELF_ID, SELF_NAME, 0,
                ":v", "06/01/2026 01:32", true, false));

        // Date divider
        list.add(new Message("7 tháng 1 năm 2026"));

        // Friend message
        list.add(new Message("4", friendId, friendName, friendAvatarRes,
                "Tui tưởng ông bay acc rồi mà vẫn lấy lại được luôn 😮",
                "07/01/2026 00:23", false, true));

        // Self message group  
        list.add(new Message("5", SELF_ID, SELF_NAME, 0,
                "ngta vào acc tui gửi tào lao à", "07/01/2026 19:04", true, true));
        list.add(new Message("6", SELF_ID, SELF_NAME, 0,
                "chứ ko có làm gì", "07/01/2026 19:04", true, false));
        list.add(new Message("7", SELF_ID, SELF_NAME, 0,
                ":v", "07/01/2026 19:10", true, false));

        return list;
    }
}
