package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

public class ServerChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_SERVER_NAME = "server_name";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private String channelName;

    // Current user ID (mock)
    private static final String SELF_ID = "me";
    private static final String SELF_NAME = "You";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_chat);

        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        if (channelName == null) channelName = "general";

        // Toolbar
        TextView tvChannelName = findViewById(R.id.tv_channel_name);
        tvChannelName.setText(channelName);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Input + send
        etMessageInput = findViewById(R.id.et_message_input);
        etMessageInput.setHint("Nhắn #" + channelName);

        ImageView btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        // RecyclerView
        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageList = buildMockServerMessages();
        messageAdapter = new MessageAdapter(messageList);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Check if last real message was also from self (for grouping)
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
     * Builds sample server chat messages similar to the Discord screenshots.
     */
    private List<Message> buildMockServerMessages() {
        List<Message> list = new ArrayList<>();

        // Date divider
        list.add(new Message("9 tháng 1 năm 2026"));

        // Group 1 – user1
        list.add(new Message("1", "user1", "Hoang Huan", R.drawable.avatar1,
                "elden-huan", "09/01/2026 22:38", false, true));
        list.add(new Message("2", "user1", "Hoang Huan", R.drawable.avatar1,
                "huan1412", "09/01/2026 22:38", false, false));

        // Continuation – user1
        list.add(new Message("3", "user1", "Hoang Huan", R.drawable.avatar1,
                "haha1412", "09/01/2026 22:53", false, false));

        // Date divider
        list.add(new Message("10 tháng 1 năm 2026"));

        // Group with image – user1
        list.add(new Message("4", "user1", "Hoang Huan", R.drawable.avatar1,
                "", "10/01/2026 19:01", false, true,
                R.drawable.img_online_meeting1));

        // Continuation – user1
        list.add(new Message("5", "user1", "Hoang Huan", R.drawable.avatar1,
                "1271146798", "10/01/2026 19:10", false, false));
        list.add(new Message("6", "user1", "Hoang Huan", R.drawable.avatar1,
                "1271146798", "10/01/2026 19:21", false, false));
        list.add(new Message("7", "user1", "Hoang Huan", R.drawable.avatar1,
                "a", "10/01/2026 19:37", false, false));

        return list;
    }
}
