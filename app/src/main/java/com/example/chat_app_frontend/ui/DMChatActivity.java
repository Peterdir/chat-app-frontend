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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");
    private static final long GROUP_BREAK_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(10);

    private String lastMessageDayKey;
    private String lastMessageSenderId;
    private long lastMessageAtMillis = -1L;

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

        long now = System.currentTimeMillis();
        String currentDayKey = buildDayKey(now);
        if (!currentDayKey.equals(lastMessageDayKey)) {
            messageList.add(new Message(formatDateDividerLabel(now)));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            lastMessageDayKey = currentDayKey;
        }

        boolean isFirst = !SELF_ID.equals(lastMessageSenderId)
                || isGroupBreak(lastMessageAtMillis, now);

        Message msg = new Message(
                String.valueOf(now),
                SELF_ID,
                SELF_NAME,
                0,
                text,
                formatMessageTimestamp(now),
                true,
                isFirst
        );

        messageList.add(msg);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etMessageInput.setText("");

        lastMessageSenderId = SELF_ID;
        lastMessageAtMillis = now;
    }

    private String formatMessageTimestamp(long millis) {
        Date date = new Date(millis);
        String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        if (isToday(millis)) {
            return "Hôm nay lúc " + timeText;
        }
        if (isYesterday(millis)) {
            return "Hôm qua lúc " + timeText;
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
    }

    private String formatDateDividerLabel(long millis) {
        String fullDate = capitalizeFirst(new SimpleDateFormat(
                "EEEE, d 'tháng' M 'năm' yyyy", VIETNAMESE_LOCALE
        ).format(new Date(millis)));
        return fullDate;
    }

    private boolean isGroupBreak(long previousMillis, long currentMillis) {
        if (previousMillis <= 0 || currentMillis <= 0) {
            return false;
        }
        return (currentMillis - previousMillis) > GROUP_BREAK_THRESHOLD_MS;
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase(VIETNAMESE_LOCALE) + text.substring(1);
    }

    private String buildDayKey(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        return year + "-" + dayOfYear;
    }

    private boolean isToday(long millis) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(millis);
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(long millis) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(millis);

        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }
}
