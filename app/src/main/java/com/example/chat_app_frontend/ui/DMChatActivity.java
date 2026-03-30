package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DMChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageInteractionListener {

    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_STATUS = "friend_status";
    public static final String EXTRA_FRIEND_AVATAR = "friend_avatar";
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
    private static final String SELF_NAME = "You";
    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");
    private static final long GROUP_BREAK_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(10);

    // PickVisualMedia để mở THƯ VIỆN ẢNH (Gallery)
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadAndSendImage(uri);
                }
            });

    private final ActivityResultLauncher<String> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAndSendFile(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_chat);

        friendName = getIntent().getStringExtra(EXTRA_FRIEND_NAME);
        String friendStatus = getIntent().getStringExtra(EXTRA_FRIEND_STATUS);
        int friendAvatarRes = getIntent().getIntExtra(EXTRA_FRIEND_AVATAR, 0);
        friendUid = getIntent().getStringExtra(EXTRA_FRIEND_UID);

        setupToolbar(friendName, friendStatus, friendAvatarRes);

        etMessageInput = findViewById(R.id.et_message_input);
        etMessageInput.setHint("Nhắn tin với @" + (friendName != null ? friendName : "bạn"));

        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
        
        // Bấm dấu cộng mở Menu đính kèm hoặc mở luôn Gallery (theo yêu cầu)
        findViewById(R.id.btn_attach).setOnClickListener(v -> {
            // Theo yêu cầu: Bấm dấu cộng truy cập thư viện ảnh
            openGallery();
        });

        // Nếu bạn muốn nút "Tệp tin" vẫn khả dụng, bạn có thể thêm một nút kẹp giấy riêng 
        // hoặc mở BottomSheet khi người dùng nhấn giữ nút cộng, v.v.
        // Ở đây tôi giả định bạn sẽ dùng một nút bấm khác để mở showAttachOptions() 
        // hoặc người dùng sẽ chọn File từ một menu khác. 
        // Để demo đúng yêu cầu: "Dấu cộng -> Gallery", tôi sẽ gắn showAttachOptions vào nút Emoji (tạm thời) hoặc nút khác nếu cần.

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            resolveCurrentUserName(currentUserId);
        }

        chatRepository = ChatRepository.getInstance();
        rvMessages = findViewById(R.id.rv_messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this, currentUserId);
        rvMessages.setAdapter(messageAdapter);

        findViewById(R.id.btn_call).setOnClickListener(v -> startPrivateCall(false));
        findViewById(R.id.btn_video_call).setOnClickListener(v -> startPrivateCall(true));

        rootLayout = findViewById(R.id.root_layout);
        if (currentUserId != null && !currentUserId.isEmpty() && friendUid != null) {
            chatId = "dm_" + CallRepository.buildCallId(currentUserId, friendUid);
        } else {
            chatId = "dm_default";
        }
        ChatThemeHelper.applyTheme(rootLayout, ChatThemeHelper.getTheme(this, chatId));
        findViewById(R.id.btn_theme).setOnClickListener(v -> showThemePicker());

        attachRealtimeMessages();
    }

    private void showAttachOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_attach_options, null);

        // Nút Tệp tin bên phải
        view.findViewById(R.id.btn_pick_file).setOnClickListener(v -> {
            dialog.dismiss();
            pickFileLauncher.launch("*/*");
        });

        // Khu vực lưới ảnh bên dưới: Khi bấm vào sẽ mở Thư viện ảnh (Gallery)
        view.findViewById(R.id.rv_photo_picker).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openGallery() {
        pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadAndSendImage(Uri uri) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        String fileName = "chat_images/" + UUID.randomUUID().toString();
        StorageReference ref = FirebaseManager.getStorageReference(fileName);

        ref.putFile(uri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            chatRepository.sendDirectMessage(chatId, currentUserId, currentUserName, "", RealtimeChatMessage.TYPE_IMAGE,
                    downloadUri.toString(), null, null, 0, new ChatRepository.OnCompleteListener() {
                @Override public void onSuccess() {}
                @Override public void onFailure(String error) { Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show(); }
            });
        })).addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadAndSendFile(Uri uri) {
        String name = "file";
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx != -1) name = cursor.getString(nameIdx);
                if (sizeIdx != -1) size = cursor.getLong(sizeIdx);
            }
        }
        final String fName = name;
        final long fSize = size;
        Toast.makeText(this, "Đang tải tệp lên...", Toast.LENGTH_SHORT).show();
        StorageReference ref = FirebaseManager.getStorageReference("chat_files/" + UUID.randomUUID() + "_" + fName);
        ref.putFile(uri).addOnSuccessListener(ts -> ref.getDownloadUrl().addOnSuccessListener(dUri -> {
            chatRepository.sendDirectMessage(chatId, currentUserId, currentUserName, "", RealtimeChatMessage.TYPE_FILE,
                    null, dUri.toString(), fName, fSize, new ChatRepository.OnCompleteListener() {
                @Override public void onSuccess() {}
                @Override public void onFailure(String error) { Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show(); }
            });
        }));
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        chatRepository.sendDirectMessage(chatId, currentUserId, currentUserName, text, new ChatRepository.OnCompleteListener() {
            @Override public void onSuccess() { etMessageInput.setText(""); }
            @Override public void onFailure(String error) { Toast.makeText(DMChatActivity.this, error, Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setupToolbar(String name, String status, int avatarRes) {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tv_friend_name)).setText(name != null ? name : "Friend");
        TextView tvStatus = findViewById(R.id.tv_friend_status);
        tvStatus.setText(status != null ? status : "Online");
        ImageView img = findViewById(R.id.img_friend_avatar);
        if (avatarRes != 0) img.setImageResource(avatarRes);
    }

    private void attachRealtimeMessages() {
        chatListener = chatRepository.observeDirectMessages(chatId, new ChatRepository.OnMessagesChangedListener() {
            @Override public void onSuccess(List<RealtimeChatMessage> messages) {
                messageList.clear();
                messageList.addAll(toUiMessages(messages));
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onFailure(String error) {}
        });
    }

    private List<Message> toUiMessages(List<RealtimeChatMessage> raw) {
        List<Message> ui = new ArrayList<>();
        Collections.sort(raw, Comparator.comparingLong(RealtimeChatMessage::getCreatedAt));
        String prevSender = null;
        for (RealtimeChatMessage r : raw) {
            boolean isFirst = r.getSenderId() != null && !r.getSenderId().equals(prevSender);
            Message m = new Message(r.getId(), r.getSenderId(), r.getSenderName(), 0, r.getContent(), 
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(r.getCreatedAt())), 
                r.getSenderId() != null && r.getSenderId().equals(currentUserId), isFirst);
            m.setMessageType(r.getMessageType());
            m.setImageUrl(r.getImageUrl());
            m.setFileUrl(r.getFileUrl());
            m.setFileName(r.getFileName());
            m.setFileSize(r.getFileSize());
            ui.add(m);
            prevSender = r.getSenderId();
        }
        return ui;
    }

    private void resolveCurrentUserName(String uid) {
        FirebaseManager.getUsersRef().child(uid).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) { if (s.exists()) currentUserName = s.getValue(String.class); }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    private void startPrivateCall(boolean isVideo) { /* Call implementation */ }
    private void showThemePicker() { /* Theme implementation */ }
    @Override public void onMessageLongPressed(Message m) {}
    @Override public void onReactionChipClicked(Message m, String e) {}

    @Override protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatRepository.removeDirectMessagesObserver(chatId, chatListener);
    }
}
