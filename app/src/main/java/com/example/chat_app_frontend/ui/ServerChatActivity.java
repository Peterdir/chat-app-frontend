package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.MessageAdapter;
import com.example.chat_app_frontend.model.Message;
import com.example.chat_app_frontend.model.RealtimeChatMessage;
import com.example.chat_app_frontend.repository.ChatRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServerChatActivity extends AppCompatActivity implements GiphyDialogFragment.GifSelectionListener {

    public static final String EXTRA_CHANNEL_ID = "channel_id";
    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_SERVER_ID = "server_id";
    public static final String EXTRA_SERVER_NAME = "server_name";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private String channelName;
        private String channelId;
        private String serverId;

        private ChatRepository chatRepository;
        private ValueEventListener chatListener;

        private String currentUserId = "";
        private String currentUserName = "You";

        // Keep constants for legacy payload formatting usage only
        private static final String SELF_NAME_FALLBACK = "You";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_chat);

        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        if (channelName == null) channelName = "general";
                channelId = getIntent().getStringExtra(EXTRA_CHANNEL_ID);
                if (channelId == null || channelId.trim().isEmpty()) channelId = channelName;
                serverId = getIntent().getStringExtra(EXTRA_SERVER_ID);
                if (serverId == null || serverId.trim().isEmpty()) serverId = "unknown_server";

                chatRepository = ChatRepository.getInstance();

                FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                if (fbUser != null) {
                        currentUserId = fbUser.getUid();
                        resolveCurrentUserName(fbUser.getUid());
                }

        // Toolbar
        TextView tvChannelName = findViewById(R.id.tv_channel_name);
        tvChannelName.setText(channelName);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Input + send
        etMessageInput = findViewById(R.id.et_message_input);
        etMessageInput.setHint("Nhắn #" + channelName);

        View btnEmoji = findViewById(R.id.btn_emoji);
        View btnGif = findViewById(R.id.btn_gif);
        View btnGift = findViewById(R.id.btn_gift);

        btnEmoji.setOnClickListener(v -> showEmojiPicker());
        btnGif.setOnClickListener(v -> showGifPicker());
        btnGift.setOnClickListener(v -> showGiftPicker());

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

                chatRepository.subscribeToServerChannelTopic(serverId, channelId);
                attachRealtimeMessages();
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

                if (currentUserId == null || currentUserId.trim().isEmpty()) {
                        Toast.makeText(this, "Bạn cần đăng nhập để gửi tin nhắn", Toast.LENGTH_SHORT).show();
                        return;
        }

                chatRepository.sendServerChannelMessage(
                                serverId,
                                channelId,
                                currentUserId,
                                currentUserName,
                                text,
                                new ChatRepository.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                                etMessageInput.setText("");
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                                Toast.makeText(ServerChatActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                }
                );
    }

        private void showEmojiPicker() {
                BottomSheetDialog dialog = new BottomSheetDialog(this);
                EmojiPickerView emojiPickerView = new EmojiPickerView(this);
                emojiPickerView.setOnEmojiPickedListener(item -> {
                        appendToInput(item.getEmoji());
                        appendToInput(" ");
                        dialog.dismiss();
                });
                dialog.setContentView(emojiPickerView);
                dialog.show();
        }

        private void showGifPicker() {
                String apiKey = getString(R.string.giphy_api_key).trim();
                if (apiKey.isEmpty() || apiKey.contains("YOUR_GIPHY")) {
                        Toast.makeText(this, "Bạn chưa cấu hình giphy_api_key trong strings.xml", Toast.LENGTH_LONG).show();
                        return;
                }

                GPHSettings settings = new GPHSettings();
                settings.setTheme(GPHTheme.Automatic);
                settings.setShowConfirmationScreen(false);
                settings.setMediaTypeConfig(new GPHContentType[]{GPHContentType.gif, GPHContentType.recents});

                GiphyDialogFragment dialog = GiphyDialogFragment.Companion.newInstance(settings, apiKey);
                dialog.setGifSelectionListener(this);
                dialog.show(getSupportFragmentManager(), "giphy_dialog");
        }

        private void showGiftPicker() {
                String[] gifts = {
                                "Nitro 1 ngày",
                                "Nitro 1 tháng",
                                "Server Boost x1",
                                "Server Boost x2"
                };
                showQuickPicker("Chọn Quà", gifts, item -> sendQuickPayload("GIFT", item));
        }

        private interface OnQuickItemSelected {
                void onSelected(String item);
        }

        private void showQuickPicker(String title, String[] items, OnQuickItemSelected onSelected) {
                BottomSheetDialog dialog = new BottomSheetDialog(this);

                LinearLayout root = new LinearLayout(this);
                root.setOrientation(LinearLayout.VERTICAL);
                int pad = dp(16);
                root.setPadding(pad, pad, pad, pad);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(title);
                tvTitle.setTextSize(18f);
                tvTitle.setTextColor(getColor(R.color.discord_text_primary));
                tvTitle.setPadding(0, 0, 0, dp(10));
                root.addView(tvTitle);

                for (String item : items) {
                        TextView row = new TextView(this);
                        row.setText(item);
                        row.setTextSize(16f);
                        row.setTextColor(getColor(R.color.discord_text_primary));
                        row.setBackgroundResource(R.drawable.bg_search_input);
                        row.setPadding(dp(12), dp(12), dp(12), dp(12));

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.bottomMargin = dp(8);
                        row.setLayoutParams(lp);

                        row.setOnClickListener(v -> {
                                onSelected.onSelected(item);
                                dialog.dismiss();
                        });
                        root.addView(row);
                }

                dialog.setContentView(root);
                dialog.show();
        }

        private void appendToInput(String value) {
                int start = Math.max(etMessageInput.getSelectionStart(), 0);
                int end = Math.max(etMessageInput.getSelectionEnd(), 0);
                etMessageInput.getText().replace(Math.min(start, end), Math.max(start, end), value, 0, value.length());
        }

        private void sendQuickPayload(String type, String payload) {
                String composed = "[" + type + "] " + payload;
                etMessageInput.setText(composed);
                etMessageInput.setSelection(composed.length());
                sendMessage();
                Toast.makeText(this, type + " đã được chèn", Toast.LENGTH_SHORT).show();
        }

        private int dp(int value) {
                return (int) (value * getResources().getDisplayMetrics().density);
        }

        @Override
        public void onGifSelected(Media media, String searchTerm, GPHContentType selectedContentType) {
                if (media == null || media.getId() == null) {
                        return;
                }
                // UI-only: gửi dạng payload để hiện trên khung chat, chưa lưu DB
                sendQuickPayload("GIF", "giphy://" + media.getId());
        }

        @Override
        public void onDismissed(GPHContentType gphContentType) {
                // Không cần xử lý
        }

        @Override
        public void didSearchTerm(String term) {
                // Không cần xử lý analytics ở bản UI-only
        }

        @Override
        protected void onDestroy() {
                super.onDestroy();
                if (chatListener != null) {
                        chatRepository.removeServerChannelObserver(serverId, channelId, chatListener);
                }
        }

        private void attachRealtimeMessages() {
                chatListener = chatRepository.observeServerChannelMessages(
                                serverId,
                                channelId,
                                new ChatRepository.OnMessagesChangedListener() {
                                        @Override
                                        public void onSuccess(List<RealtimeChatMessage> messages) {
                                                List<Message> uiMessages = toUiMessages(messages);
                                                messageList.clear();
                                                messageList.addAll(uiMessages);
                                                messageAdapter.notifyDataSetChanged();
                                                if (!messageList.isEmpty()) {
                                                        rvMessages.scrollToPosition(messageList.size() - 1);
                                                }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                                Toast.makeText(ServerChatActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                }
                );
        }

        private List<Message> toUiMessages(List<RealtimeChatMessage> rawMessages) {
                List<Message> uiMessages = new ArrayList<>();
                String previousSenderId = null;
                for (RealtimeChatMessage raw : rawMessages) {
                        String senderId = raw.getSenderId() != null ? raw.getSenderId() : "unknown";
                        String senderName = raw.getSenderName() != null && !raw.getSenderName().trim().isEmpty()
                                        ? raw.getSenderName() : SELF_NAME_FALLBACK;
                        boolean isSelf = senderId.equals(currentUserId);
                        boolean isFirstInGroup = !senderId.equals(previousSenderId);
                        previousSenderId = senderId;

                        String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(new Date(raw.getCreatedAt()));

                        Message ui = new Message(
                                        raw.getId() != null ? raw.getId() : String.valueOf(raw.getCreatedAt()),
                                        senderId,
                                        senderName,
                                        0,
                                        raw.getContent() != null ? raw.getContent() : "",
                                        "Hôm nay lúc " + timeText,
                                        isSelf,
                                        isFirstInGroup
                        );
                        uiMessages.add(ui);
                }
                return uiMessages;
        }

        private void resolveCurrentUserName(String uid) {
                FirebaseManager.getUsersRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                                String displayName = snapshot.child("displayName").getValue(String.class);
                                String userName = snapshot.child("userName").getValue(String.class);
                                if (displayName != null && !displayName.trim().isEmpty()) {
                                        currentUserName = displayName;
                                        return;
                                }
                                if (userName != null && !userName.trim().isEmpty()) {
                                        currentUserName = userName;
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                                // Keep fallback name
                        }
                });
    }
}
