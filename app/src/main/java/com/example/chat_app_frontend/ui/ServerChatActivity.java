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
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
                showQuickPicker("Chọn Quà (mock)", gifts, item -> sendQuickPayload("GIFT", item));
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
                Toast.makeText(this, type + " đã được chèn (mock)", Toast.LENGTH_SHORT).show();
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
