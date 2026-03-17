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

import java.util.ArrayList;
import java.util.List;

public class DMChatActivity extends AppCompatActivity {

    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_STATUS = "friend_status";
    public static final String EXTRA_FRIEND_AVATAR = "friend_avatar";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private String friendName;

    private static final String SELF_ID = "self";
    private static final String SELF_NAME = "You";

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

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        rvMessages.setAdapter(messageAdapter);

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
            SELF_ID, SELF_NAME, 0, text, "Vừa xong",
                true, isFirst
        );
        messageList.add(msg);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etMessageInput.setText("");
    }
}
