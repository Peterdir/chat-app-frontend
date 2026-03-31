package com.example.chat_app_frontend.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.MessageAdapter;
import com.example.chat_app_frontend.adapter.ServerMemberAdapter;
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.Message;
import com.example.chat_app_frontend.model.RealtimeChatMessage;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.ChatRepository;
import com.example.chat_app_frontend.utils.ChatThemeHelper;
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

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerChatActivity extends AppCompatActivity
        implements GiphyDialogFragment.GifSelectionListener, MessageAdapter.OnMessageInteractionListener, VoiceStateManager.VoiceStateListener {

    public static final String EXTRA_CHANNEL_ID = "channel_id";
    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_SERVER_ID = "server_id";
    public static final String EXTRA_SERVER_NAME = "server_name";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
        private LinearLayout llReplyPreview;
        private TextView tvReplyPreviewSender;
        private TextView tvReplyPreviewContent;
        private ImageView btnCancelReply;
    private String channelName;
        private String channelId;
        private String serverId;

        private ChatRepository chatRepository;
        private ValueEventListener chatListener;

        private String currentUserId = "";
        private String currentUserName = "You";
        private Message pendingReplyMessage;
        private ConstraintLayout rootLayout;
        private String chatThemeId;

        // Keep constants for legacy payload formatting usage only
        private static final String SELF_NAME_FALLBACK = "You";
        private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");
        private static final long GROUP_BREAK_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(10);
        private static final String[] QUICK_REACTION_EMOJIS = {"❤️", "😆", "😮", "😢", "😡", "👍", "👎"};

        // Voice Bar UI
        private View globalVoiceBar;
        private TextView tvVoiceBarChannel;
        private TextView tvVoiceBarTimer;
        private ImageView btnVoiceBarMic;
        private ImageView btnVoiceBarDisconnect;
        private Handler voiceTimerHandler = new Handler(Looper.getMainLooper());
        private Runnable voiceTimerRunnable;

        private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_chat);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageMessage(uri);
                    }
                });

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

        View btnMembers = findViewById(R.id.btn_members);
        btnMembers.setOnClickListener(v -> showServerMembersBottomSheet());

        // Input + send
        etMessageInput = findViewById(R.id.et_message_input);
        llReplyPreview = findViewById(R.id.ll_reply_preview);
        tvReplyPreviewSender = findViewById(R.id.tv_reply_preview_sender);
        tvReplyPreviewContent = findViewById(R.id.tv_reply_preview_content);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);
        btnCancelReply.setOnClickListener(v -> clearReplyTarget());
        clearReplyTarget();
        etMessageInput.setHint("Nhắn #" + channelName);

        View btnEmoji = findViewById(R.id.btn_emoji);
        View btnGif = findViewById(R.id.btn_gif);
        View btnGift = findViewById(R.id.btn_gift);

        btnEmoji.setOnClickListener(v -> showEmojiPicker());
        btnGif.setOnClickListener(v -> showGifPicker());
        btnGift.setOnClickListener(v -> showGiftPicker());

        ImageView btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        ImageView btnAttach = findViewById(R.id.btn_attach);
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        // RecyclerView
        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

                messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this, currentUserId);
        rvMessages.setAdapter(messageAdapter);

                chatRepository.subscribeToServerChannelTopic(serverId, channelId);
                attachRealtimeMessages();

        // Theme
        rootLayout = findViewById(R.id.root_layout);
        chatThemeId = serverId + "_" + channelId;
        int savedTheme = ChatThemeHelper.getTheme(this, chatThemeId);
        ChatThemeHelper.applyTheme(rootLayout, savedTheme);

        ImageView btnTheme = findViewById(R.id.btn_theme);
        btnTheme.setOnClickListener(v -> showThemePicker());

        initGlobalVoiceBar();
        VoiceStateManager.getInstance().addListener(this);
    }

    private void uploadImageMessage(Uri imageUri) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để gửi ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) return;
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                if (originalBitmap == null) return;

                int maxSize = 800;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                Bitmap resizedBitmap = originalBitmap;
                if (width > maxSize || height > maxSize) {
                    float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                    int newWidth = Math.round(width * ratio);
                    int newHeight = Math.round(height * ratio);
                    resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                baos.close();

                String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                String imageData = "data:image/jpeg;base64," + base64String;

                if (resizedBitmap != originalBitmap) {
                    resizedBitmap.recycle();
                }
                originalBitmap.recycle();

                runOnUiThread(() -> {
                    chatRepository.sendServerChannelMessage(
                            serverId,
                            channelId,
                            channelName,
                            currentUserId,
                            currentUserName,
                            imageData,
                            pendingReplyMessage != null ? pendingReplyMessage.getId() : null,
                            pendingReplyMessage != null ? pendingReplyMessage.getSenderName() : null,
                            pendingReplyMessage != null ? buildReplySnippetOrNull(pendingReplyMessage.getContent()) : null,
                            new ChatRepository.OnCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    clearReplyTarget();
                                }
                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(ServerChatActivity.this, "Lỗi gửi ảnh: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(ServerChatActivity.this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                        channelName,
                                currentUserId,
                                currentUserName,
                                text,
                                pendingReplyMessage != null ? pendingReplyMessage.getId() : null,
                                pendingReplyMessage != null ? pendingReplyMessage.getSenderName() : null,
                                pendingReplyMessage != null ? buildReplySnippetOrNull(pendingReplyMessage.getContent()) : null,
                                new ChatRepository.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                                etMessageInput.setText("");
                                                clearReplyTarget();
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

        private void showServerMembersBottomSheet() {
                BottomSheetDialog dialog = new BottomSheetDialog(this);
                View content = getLayoutInflater().inflate(R.layout.bottom_sheet_server_members, null, false);
                dialog.setContentView(content);

                ImageView btnClose = content.findViewById(R.id.btn_close_members_sheet);
                RecyclerView rvMembers = content.findViewById(R.id.rv_server_members);
                View progress = content.findViewById(R.id.progress_members);
                TextView tvEmpty = content.findViewById(R.id.tv_members_empty);

                btnClose.setOnClickListener(v -> dialog.dismiss());

                rvMembers.setLayoutManager(new LinearLayoutManager(this));
                ServerMemberAdapter adapter = new ServerMemberAdapter();
                rvMembers.setAdapter(adapter);

                loadServerMembers(adapter, progress, tvEmpty);
                dialog.show();
        }

        private void loadServerMembers(ServerMemberAdapter adapter, View progress, TextView tvEmpty) {
                progress.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);

                FirebaseManager.getDatabaseReference("server_members")
                                .child(serverId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                                if (!snapshot.exists()) {
                                                        progress.setVisibility(View.GONE);
                                                        tvEmpty.setText("Server chưa có thành viên");
                                                        tvEmpty.setVisibility(View.VISIBLE);
                                                        adapter.submitList(new ArrayList<>());
                                                        return;
                                                }

                                                List<String> memberUids = new ArrayList<>();
                                                for (DataSnapshot child : snapshot.getChildren()) {
                                                        if (child.getKey() != null && !child.getKey().trim().isEmpty()) {
                                                                memberUids.add(child.getKey());
                                                        }
                                                }

                                                if (memberUids.isEmpty()) {
                                                        progress.setVisibility(View.GONE);
                                                        tvEmpty.setText("Server chưa có thành viên");
                                                        tvEmpty.setVisibility(View.VISIBLE);
                                                        adapter.submitList(new ArrayList<>());
                                                        return;
                                                }

                                                List<User> members = new ArrayList<>();
                                                int[] remain = {memberUids.size()};

                                                for (String uid : memberUids) {
                                                        FirebaseManager.getUsersRef()
                                                                        .child(uid)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot userSnap) {
                                                                                        User user = userSnap.getValue(User.class);
                                                                                        if (user != null) {
                                                                                                if (user.getFirebaseUid() == null || user.getFirebaseUid().trim().isEmpty()) {
                                                                                                        user.setFirebaseUid(uid);
                                                                                                }
                                                                                                members.add(user);
                                                                                        }
                                                                                        onMemberProfileLoaded(remain, members, adapter, progress, tvEmpty);
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError error) {
                                                                                        onMemberProfileLoaded(remain, members, adapter, progress, tvEmpty);
                                                                                }
                                                                        });
                                                }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                                progress.setVisibility(View.GONE);
                                                tvEmpty.setText("Không tải được danh sách thành viên");
                                                tvEmpty.setVisibility(View.VISIBLE);
                                                adapter.submitList(new ArrayList<>());
                                        }
                                });
        }

        private void onMemberProfileLoaded(int[] remain,
                                           List<User> members,
                                           ServerMemberAdapter adapter,
                                           View progress,
                                           TextView tvEmpty) {
                remain[0] = remain[0] - 1;
                if (remain[0] > 0) {
                        return;
                }

                members.sort((a, b) -> {
                        String an = a.getDisplayNameOrUserName() != null ? a.getDisplayNameOrUserName() : "";
                        String bn = b.getDisplayNameOrUserName() != null ? b.getDisplayNameOrUserName() : "";
                        return an.compareToIgnoreCase(bn);
                });

                adapter.submitList(members);
                progress.setVisibility(View.GONE);

                if (members.isEmpty()) {
                        tvEmpty.setText("Không tải được hồ sơ thành viên");
                        tvEmpty.setVisibility(View.VISIBLE);
                } else {
                        tvEmpty.setVisibility(View.GONE);
                }
        }

        private void showMessageActionSheet(Message message) {
                BottomSheetDialog dialog = new BottomSheetDialog(this);

                LinearLayout root = new LinearLayout(this);
                root.setOrientation(LinearLayout.VERTICAL);
                int pad = dp(16);
                root.setPadding(pad, pad, pad, pad);

                TextView tvTitle = new TextView(this);
                tvTitle.setText("Thả cảm xúc");
                tvTitle.setTextSize(17f);
                tvTitle.setTextColor(getColor(R.color.discord_text_primary));
                tvTitle.setPadding(0, 0, 0, dp(12));
                root.addView(tvTitle);

                HorizontalScrollView scrollView = new HorizontalScrollView(this);
                scrollView.setHorizontalScrollBarEnabled(false);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);

                for (String emoji : QUICK_REACTION_EMOJIS) {
                        TextView chip = new TextView(this);
                        chip.setText(emoji);
                        chip.setTextSize(24f);
                        chip.setGravity(Gravity.CENTER);
                        chip.setBackgroundResource(R.drawable.bg_reaction_option);
                        chip.setPadding(dp(14), dp(8), dp(14), dp(8));

                        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        chipLp.setMarginEnd(dp(8));
                        chip.setLayoutParams(chipLp);

                        chip.setOnClickListener(v -> {
                                toggleReaction(message, emoji);
                                dialog.dismiss();
                        });
                        row.addView(chip);
                }

                scrollView.addView(row);
                LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                );
                scrollLp.bottomMargin = dp(12);
                scrollView.setLayoutParams(scrollLp);
                root.addView(scrollView);

                boolean isOwnMessage = isMessageOwnedByCurrentUser(message);

                addActionRow(root, "Trả lời tin nhắn", () -> {
                        dialog.dismiss();
                        setReplyTarget(message);
                });

                if (isOwnMessage) {
                        addActionRow(root, "Sửa tin nhắn", () -> {
                                dialog.dismiss();
                                showEditMessageDialog(message);
                        });
                        addActionRow(root, "Xóa tin nhắn", () -> {
                                dialog.dismiss();
                                confirmDeleteMessage(message);
                        });
                }

                dialog.setContentView(root);
                dialog.show();
        }

        private void setReplyTarget(Message message) {
                if (message == null || Message.TYPE_DATE_DIVIDER.equals(message.getMessageType())) {
                        return;
                }
                pendingReplyMessage = message;
                llReplyPreview.setVisibility(View.VISIBLE);
                tvReplyPreviewSender.setText(
                                message.getSenderName() != null && !message.getSenderName().trim().isEmpty()
                                                ? message.getSenderName()
                                                : "Tin nhắn"
                );
                tvReplyPreviewContent.setText(buildReplyPreviewSnippet(message.getContent()));
                etMessageInput.requestFocus();
        }

        private void clearReplyTarget() {
                pendingReplyMessage = null;
                llReplyPreview.setVisibility(View.GONE);
                tvReplyPreviewSender.setText("");
                tvReplyPreviewContent.setText("");
        }

        private String buildReplyPreviewSnippet(String content) {
                if (content == null || content.trim().isEmpty()) {
                        return "Tin nhắn không có nội dung";
                }
                String normalized = content.replace('\n', ' ').trim();
                if (normalized.length() > 90) {
                        return normalized.substring(0, 90) + "...";
                }
                return normalized;
        }

        private String buildReplySnippetOrNull(String content) {
                if (content == null || content.trim().isEmpty()) {
                        return null;
                }
                String normalized = content.replace('\n', ' ').trim();
                if (normalized.length() > 90) {
                        return normalized.substring(0, 90) + "...";
                }
                return normalized;
        }

        private boolean isMessageOwnedByCurrentUser(Message message) {
                if (message == null) {
                        return false;
                }
                if (message.isSelf()) {
                        return true;
                }

                String myUid = currentUserId != null ? currentUserId.trim() : "";
                String senderUid = message.getSenderId() != null ? message.getSenderId().trim() : "";
                return !myUid.isEmpty() && myUid.equals(senderUid);
        }

        private void addActionRow(LinearLayout parent, String title, Runnable action) {
                TextView row = new TextView(this);
                row.setText(title);
                row.setTextSize(16f);
                row.setTextColor(getColor(R.color.discord_text_primary));
                row.setBackgroundResource(R.drawable.bg_search_input);
                row.setPadding(dp(12), dp(12), dp(12), dp(12));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.bottomMargin = dp(8);
                row.setLayoutParams(lp);

                row.setOnClickListener(v -> action.run());
                parent.addView(row);
        }

        private void showEditMessageDialog(Message message) {
                if (message == null || message.getId() == null || message.getId().trim().isEmpty()) {
                        return;
                }

                EditText input = new EditText(this);
                input.setText(message.getContent() != null ? message.getContent() : "");
                input.setHint("Nhập nội dung mới");
                int pad = dp(16);
                input.setPadding(pad, pad, pad, pad);

                new AlertDialog.Builder(this)
                                .setTitle("Sửa tin nhắn")
                                .setView(input)
                                .setNegativeButton("Hủy", null)
                                .setPositiveButton("Lưu", (dialog, which) -> {
                                        String newContent = input.getText().toString().trim();
                                        if (newContent.isEmpty()) {
                                                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                                                return;
                                        }

                                        chatRepository.updateMessageContent(
                                                        serverId,
                                                        channelId,
                                                        message.getId(),
                                                        newContent,
                                                        new ChatRepository.OnCompleteListener() {
                                                                @Override
                                                                public void onSuccess() {
                                                                        Toast.makeText(ServerChatActivity.this, "Đã sửa tin nhắn", Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {
                                                                        Toast.makeText(ServerChatActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                }
                                                        }
                                        );
                                })
                                .show();
        }

        private void confirmDeleteMessage(Message message) {
                if (message == null || message.getId() == null || message.getId().trim().isEmpty()) {
                        return;
                }

                new AlertDialog.Builder(this)
                                .setTitle("Xóa tin nhắn")
                                .setMessage("Bạn có chắc muốn xóa tin nhắn này không?")
                                .setNegativeButton("Hủy", null)
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                        chatRepository.deleteMessage(
                                                        serverId,
                                                        channelId,
                                                        message.getId(),
                                                        new ChatRepository.OnCompleteListener() {
                                                                @Override
                                                                public void onSuccess() {
                                                                        Toast.makeText(ServerChatActivity.this, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {
                                                                        Toast.makeText(ServerChatActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                }
                                                        }
                                        );
                                })
                                .show();
        }

        private void toggleReaction(Message message, String emoji) {
                if (message == null || message.getId() == null || message.getId().trim().isEmpty()) {
                        return;
                }
                if (currentUserId == null || currentUserId.trim().isEmpty()) {
                        Toast.makeText(this, "Bạn cần đăng nhập để thả cảm xúc", Toast.LENGTH_SHORT).show();
                        return;
                }

                chatRepository.toggleMessageReaction(
                                serverId,
                                channelId,
                                message.getId(),
                                emoji,
                                currentUserId,
                                new ChatRepository.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                                // Realtime listener will refresh UI.
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                                Toast.makeText(ServerChatActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                }
                );
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
                VoiceStateManager.getInstance().removeListener(this);
                if (voiceTimerRunnable != null) {
                    voiceTimerHandler.removeCallbacks(voiceTimerRunnable);
                }
        }

        private void initGlobalVoiceBar() {
            globalVoiceBar = findViewById(R.id.global_voice_bar_stub);
            if (globalVoiceBar == null) {
                // Not inflated maybe? Try direct layout ID if it's not a stub
                globalVoiceBar = findViewById(R.id.global_voice_bar);
                if (globalVoiceBar == null) return;
            }
            tvVoiceBarChannel = globalVoiceBar.findViewById(R.id.tv_voice_bar_channel);
            tvVoiceBarTimer = globalVoiceBar.findViewById(R.id.tv_voice_bar_timer);
            btnVoiceBarMic = globalVoiceBar.findViewById(R.id.btn_voice_bar_mic);
            btnVoiceBarDisconnect = globalVoiceBar.findViewById(R.id.btn_voice_bar_disconnect);

            View infoClickArea = globalVoiceBar.findViewById(R.id.ll_voice_bar_info);
            infoClickArea.setOnClickListener(v -> {
                String cName = VoiceStateManager.getInstance().getConnectedChannelName();
                if (cName != null) {
                    android.content.Intent intent = new android.content.Intent(this, VoiceChannelActivity.class);
                    // Open existing voice activity
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });

            btnVoiceBarMic.setOnClickListener(v -> {
                boolean isMuted = VoiceStateManager.getInstance().isMuted();
                VoiceStateManager.getInstance().setMuted(!isMuted);
                // Also notify Agora Engine ideally... Note: we only update the manager here.
                // An ideal architecture uses an application level service for Agora so it catches this toggle.
                btnVoiceBarMic.setImageResource(!isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
                btnVoiceBarMic.setColorFilter(!isMuted ? android.graphics.Color.parseColor("#ED4245") : android.graphics.Color.parseColor("#DBDEE1"));
            });

            btnVoiceBarDisconnect.setOnClickListener(v -> {
                VoiceStateManager.getInstance().leaveChannel();
            });

            voiceTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    updateVoiceTimer();
                    voiceTimerHandler.postDelayed(this, 1000);
                }
            };
            onVoiceStateChanged(); // update initially
        }

        @Override
        public void onVoiceStateChanged() {
            if (globalVoiceBar == null) return;
            String connectedChannel = VoiceStateManager.getInstance().getConnectedChannelName();
            
            if (connectedChannel == null || connectedChannel.trim().isEmpty()) {
                globalVoiceBar.setVisibility(View.GONE);
                voiceTimerHandler.removeCallbacks(voiceTimerRunnable);
            } else {
                globalVoiceBar.setVisibility(View.VISIBLE);
                tvVoiceBarChannel.setText("Voice / " + connectedChannel);
                
                boolean isMuted = VoiceStateManager.getInstance().isMuted();
                btnVoiceBarMic.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
                btnVoiceBarMic.setColorFilter(isMuted ? android.graphics.Color.parseColor("#ED4245") : android.graphics.Color.parseColor("#DBDEE1"));

                voiceTimerHandler.removeCallbacks(voiceTimerRunnable);
                voiceTimerHandler.post(voiceTimerRunnable);
            }
        }

        private void updateVoiceTimer() {
            if (tvVoiceBarTimer == null) return;
            long joinTime = VoiceStateManager.getInstance().getJoinTimeMillis();
            if (joinTime > 0) {
                long diff = System.currentTimeMillis() - joinTime;
                long seconds = (diff / 1000) % 60;
                long minutes = (diff / (1000 * 60)) % 60;
                long hours = (diff / (1000 * 60 * 60));
                if (hours > 0) {
                    tvVoiceBarTimer.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
                } else {
                    tvVoiceBarTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                }
            } else {
                tvVoiceBarTimer.setText("00:00");
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
                List<RealtimeChatMessage> sortedMessages = new ArrayList<>(rawMessages);
                Collections.sort(sortedMessages, Comparator.comparingLong(RealtimeChatMessage::getCreatedAt));
                Map<String, RealtimeChatMessage> messageById = new HashMap<>();
                for (RealtimeChatMessage raw : sortedMessages) {
                        if (raw.getId() != null && !raw.getId().trim().isEmpty()) {
                                messageById.put(raw.getId(), raw);
                        }
                }

                String previousSenderId = null;
                String previousDayKey = null;
                long previousMessageAt = -1L;

                for (RealtimeChatMessage raw : sortedMessages) {
                        long createdAt = raw.getCreatedAt() > 0 ? raw.getCreatedAt() : System.currentTimeMillis();
                        String currentDayKey = buildDayKey(createdAt);

                        if (!currentDayKey.equals(previousDayKey)) {
                                uiMessages.add(new Message(formatDateDividerLabel(createdAt)));
                                previousDayKey = currentDayKey;
                                previousSenderId = null;
                                previousMessageAt = -1L;
                        }

                        String senderId = raw.getSenderId() != null ? raw.getSenderId() : "unknown";
                        String senderName = raw.getSenderName() != null && !raw.getSenderName().trim().isEmpty()
                                        ? raw.getSenderName() : SELF_NAME_FALLBACK;
                        boolean isSelf = senderId.equals(currentUserId);
                        boolean isFirstInGroup = !senderId.equals(previousSenderId)
                                        || isGroupBreak(previousMessageAt, createdAt);
                        previousSenderId = senderId;
                        previousMessageAt = createdAt;

                        Message ui = new Message(
                                        raw.getId() != null ? raw.getId() : String.valueOf(createdAt),
                                        senderId,
                                        senderName,
                                        0,
                                        raw.getContent() != null ? raw.getContent() : "",
                                        formatMessageTimestamp(createdAt),
                                        isSelf,
                                        isFirstInGroup
                        );
                        ui.setReactions(raw.getReactions());

                        String replyMessageId = raw.getReplyToMessageId();
                        String replySenderName = raw.getReplyToSenderName();
                        String replyContent = raw.getReplyToContent();
                        boolean hasReply = (replyMessageId != null && !replyMessageId.trim().isEmpty())
                                || (replySenderName != null && !replySenderName.trim().isEmpty())
                                || (replyContent != null && !replyContent.trim().isEmpty());

                        if (!hasReply) {
                                ui.setReplyToMessageId(null);
                                ui.setReplyToSenderName(null);
                                ui.setReplyToContent(null);
                                uiMessages.add(ui);
                                continue;
                        }

                        if ((replySenderName == null || replySenderName.trim().isEmpty()
                                || replyContent == null || replyContent.trim().isEmpty())
                                && replyMessageId != null
                                && !replyMessageId.trim().isEmpty()) {
                                RealtimeChatMessage repliedMessage = messageById.get(replyMessageId);
                                if (repliedMessage != null) {
                                        if (replySenderName == null || replySenderName.trim().isEmpty()) {
                                                replySenderName = repliedMessage.getSenderName();
                                        }
                                        if (replyContent == null || replyContent.trim().isEmpty()) {
                                                replyContent = repliedMessage.getContent();
                                        }
                                }
                        }

                        ui.setReplyToMessageId(replyMessageId);
                        ui.setReplyToSenderName(replySenderName);
                        ui.setReplyToContent(buildReplySnippetOrNull(replyContent));
                        uiMessages.add(ui);
                }
                return uiMessages;
        }

        @Override
        public void onMessageLongPressed(Message message) {
                if (message == null || Message.TYPE_DATE_DIVIDER.equals(message.getMessageType())) {
                        return;
                }
                showMessageActionSheet(message);
        }

        @Override
        public void onReactionChipClicked(Message message, String emoji) {
                toggleReaction(message, emoji);
        }

        @Override
        public void onUserClicked(String userId) {
                if (userId == null || userId.trim().isEmpty()) return;
                UserProfileBottomSheet bottomSheet = new UserProfileBottomSheet(null, userId);
                bottomSheet.show(getSupportFragmentManager(), "user_profile");
        }

        @Override
        public void onImageClicked(String imageUrlOrBase64) {
                if (imageUrlOrBase64 == null || imageUrlOrBase64.trim().isEmpty()) return;
                com.example.chat_app_frontend.utils.ImageViewerDialog.show(this, imageUrlOrBase64);
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

    private void showThemePicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_chat_theme, null, false);
        dialog.setContentView(content);

        LinearLayout llSwatches = content.findViewById(R.id.ll_theme_swatches);
        int currentTheme = ChatThemeHelper.getTheme(this, chatThemeId);
        int swatchSize = dp(56);
        int margin = dp(8);

        for (int i = 0; i < ChatThemeHelper.THEME_PREVIEW_COLORS.length; i++) {
            final int index = i;

            // Outer frame for selection ring
            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(swatchSize, swatchSize);
            frameLp.setMarginEnd(margin);
            frame.setLayoutParams(frameLp);

            // Inner circle swatch
            View swatch = new View(this);
            int inset = dp(4);
            FrameLayout.LayoutParams swatchLp = new FrameLayout.LayoutParams(
                    swatchSize - inset * 2, swatchSize - inset * 2);
            swatchLp.setMargins(inset, inset, inset, inset);
            swatch.setLayoutParams(swatchLp);

            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(ChatThemeHelper.THEME_PREVIEW_COLORS[i]);
            swatch.setBackground(circle);

            frame.addView(swatch);

            // Selection ring
            if (i == currentTheme) {
                frame.setForeground(getDrawable(R.drawable.bg_theme_selected_ring));
            }

            frame.setOnClickListener(v -> {
                ChatThemeHelper.saveTheme(this, chatThemeId, index);
                ChatThemeHelper.applyTheme(rootLayout, index);
                dialog.dismiss();
            });

            llSwatches.addView(frame);
        }

        dialog.show();
    }
}
