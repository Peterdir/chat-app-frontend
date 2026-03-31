package com.example.chat_app_frontend.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.MessageAdapter;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.model.Message;
import com.example.chat_app_frontend.model.RealtimeChatMessage;
import com.example.chat_app_frontend.repository.CallRepository;
import com.example.chat_app_frontend.repository.ChatRepository;
import com.example.chat_app_frontend.utils.ChatThemeHelper;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

public class DMChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageInteractionListener {

    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_STATUS = "friend_status";
    public static final String EXTRA_FRIEND_AVATAR = "friend_avatar";
    public static final String EXTRA_FRIEND_AVATAR_URL = "friend_avatar_url";
    public static final String EXTRA_FRIEND_UID = "friend_uid";

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private String friendName;
    private ConstraintLayout rootLayout;
    private String chatId;
    private String friendUid;
    private String currentUserId = "";
    private String currentUserName = "You";
    private ChatRepository chatRepository;
    private ValueEventListener chatListener;

    private static final String[] QUICK_REACTION_EMOJIS = {"❤️", "😆", "😮", "😢", "😡", "👍", "👎"};

    private static final String SELF_ID = "self";
    private static final String SELF_NAME = "You";
    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");
    private static final long GROUP_BREAK_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(10);

    private String lastMessageDayKey;
    private String lastMessageSenderId;
    private long lastMessageAtMillis = -1L;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_chat);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageMessage(uri);
                    }
                });

        friendName = getIntent().getStringExtra(EXTRA_FRIEND_NAME);
        String friendStatus = getIntent().getStringExtra(EXTRA_FRIEND_STATUS);
        int friendAvatarRes = getIntent().getIntExtra(EXTRA_FRIEND_AVATAR, 0);
        String friendAvatarUrl = getIntent().getStringExtra(EXTRA_FRIEND_AVATAR_URL);
        if (friendName == null) friendName = "Friend";
        if (friendStatus == null) friendStatus = "Online";
        friendUid = getIntent().getStringExtra(EXTRA_FRIEND_UID);

        // Toolbar
        setupToolbar(friendName, friendStatus, friendAvatarRes, friendAvatarUrl);

        // Input
        etMessageInput = findViewById(R.id.et_message_input);
        etMessageInput.setHint("Nhắn tin với @" + friendName);

        ImageView btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        ImageView btnAttach = findViewById(R.id.btn_attach);
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            resolveCurrentUserName(currentUserId);
        }

        chatRepository = ChatRepository.getInstance();

        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this, currentUserId);
        rvMessages.setAdapter(messageAdapter);

        // Call buttons
        ImageView btnCall = findViewById(R.id.btn_call);
        ImageView btnVideoCall = findViewById(R.id.btn_video_call);

        btnCall.setOnClickListener(v -> startPrivateCall(false));
        btnVideoCall.setOnClickListener(v -> startPrivateCall(true));

        // Theme
        rootLayout = findViewById(R.id.root_layout);
        if (!currentUserId.trim().isEmpty() && friendUid != null && !friendUid.trim().isEmpty()) {
            chatId = "dm_" + CallRepository.buildCallId(currentUserId, friendUid);
        } else {
            chatId = "dm_" + friendName;
        }
        int savedTheme = ChatThemeHelper.getTheme(this, chatId);
        ChatThemeHelper.applyTheme(rootLayout, savedTheme);

        ImageView btnTheme = findViewById(R.id.btn_theme);
        btnTheme.setOnClickListener(v -> showThemePicker());

        attachRealtimeMessages();
    }

    private void startPrivateCall(boolean isVideo) {
        String myUid = AuthManager.getInstance(this).getUid();
        if (myUid != null && friendUid != null && !friendUid.trim().isEmpty()) {
            String callId = CallRepository.buildCallId(myUid, friendUid);
            CallRepository.getInstance().createRingingSession(callId, myUid, friendUid, isVideo);
        }
        android.content.Intent intent = new android.content.Intent(this, PrivateCallActivity.class);
        intent.putExtra(EXTRA_FRIEND_NAME, friendName);
        intent.putExtra("is_video", isVideo);
        intent.putExtra(EXTRA_FRIEND_UID, friendUid);
        intent.putExtra("is_caller", true);
        startActivity(intent);
    }

    private void setupToolbar(String name, String status, int avatarRes, String avatarUrl) {
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
        
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .into(imgAvatar);
            imgAvatar.setVisibility(View.VISIBLE);
            tvInitial.setVisibility(View.GONE);
        } else if (avatarRes != 0) {
            imgAvatar.setImageResource(avatarRes);
            imgAvatar.setVisibility(View.VISIBLE);
            tvInitial.setVisibility(View.GONE);
        } else {
            imgAvatar.setVisibility(View.GONE);
            tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
            tvInitial.setVisibility(View.VISIBLE);
        }
        
        View toolbarInfo = findViewById(R.id.ll_friend_info);
        if (toolbarInfo != null) {
            toolbarInfo.setOnClickListener(v -> onUserClicked(friendUid));
        }
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
                    chatRepository.sendDirectMessage(
                            chatId,
                            currentUserId,
                            currentUserName,
                            imageData,
                            new ChatRepository.OnCompleteListener() {
                                @Override
                                public void onSuccess() {}
                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(DMChatActivity.this, "Lỗi gửi ảnh: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(DMChatActivity.this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show());
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

        chatRepository.sendDirectMessage(
                chatId,
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
                        Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void attachRealtimeMessages() {
        chatListener = chatRepository.observeDirectMessages(chatId, new ChatRepository.OnMessagesChangedListener() {
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
                Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Message> toUiMessages(List<RealtimeChatMessage> rawMessages) {
        List<Message> uiMessages = new ArrayList<>();
        List<RealtimeChatMessage> sortedMessages = new ArrayList<>(rawMessages);
        Collections.sort(sortedMessages, Comparator.comparingLong(RealtimeChatMessage::getCreatedAt));

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
                    ? raw.getSenderName() : SELF_NAME;
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
        root.addView(row);

        if (isMessageOwnedByCurrentUser(message)) {
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

    private boolean isMessageOwnedByCurrentUser(Message message) {
        if (message == null) return false;
        String myUid = currentUserId != null ? currentUserId.trim() : "";
        String senderUid = message.getSenderId() != null ? message.getSenderId().trim() : "";
        return !myUid.isEmpty() && myUid.equals(senderUid);
    }

    private void toggleReaction(Message message, String emoji) {
        if (message == null || message.getId() == null || message.getId().trim().isEmpty()) {
            return;
        }
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để thả cảm xúc", Toast.LENGTH_SHORT).show();
            return;
        }
        chatRepository.toggleDirectMessageReaction(
                chatId,
                message.getId(),
                emoji,
                currentUserId,
                new ChatRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showEditMessageDialog(Message message) {
        if (message == null || message.getId() == null || message.getId().trim().isEmpty()) {
            return;
        }
        EditText input = new EditText(this);
        input.setText(message.getContent() != null ? message.getContent() : "");
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
                    chatRepository.updateDirectMessageContent(
                            chatId,
                            message.getId(),
                            newContent,
                            new ChatRepository.OnCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(DMChatActivity.this, "Đã sửa tin nhắn", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show();
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
                .setPositiveButton("Xóa", (dialog, which) -> chatRepository.deleteDirectMessage(
                        chatId,
                        message.getId(),
                        new ChatRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(DMChatActivity.this, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .show();
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
        lp.topMargin = dp(8);
        row.setLayoutParams(lp);
        row.setOnClickListener(v -> action.run());
        parent.addView(row);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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
            }
        });
    }

    private void showThemePicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_chat_theme, null, false);
        dialog.setContentView(content);

        LinearLayout llSwatches = content.findViewById(R.id.ll_theme_swatches);
        int currentTheme = ChatThemeHelper.getTheme(this, chatId);
        int swatchSize = (int) (56 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < ChatThemeHelper.THEME_PREVIEW_COLORS.length; i++) {
            final int index = i;

            // Outer frame for selection ring
            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(swatchSize, swatchSize);
            frameLp.setMarginEnd(margin);
            frame.setLayoutParams(frameLp);

            // Inner circle swatch
            View swatch = new View(this);
            int inset = (int) (4 * getResources().getDisplayMetrics().density);
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
                ChatThemeHelper.saveTheme(this, chatId, index);
                ChatThemeHelper.applyTheme(rootLayout, index);
                dialog.dismiss();
            });

            llSwatches.addView(frame);
        }

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRepository != null && chatListener != null) {
            chatRepository.removeDirectMessagesObserver(chatId, chatListener);
            chatListener = null;
        }
    }
}
