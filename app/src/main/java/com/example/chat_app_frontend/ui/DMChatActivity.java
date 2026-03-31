package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    // UI for progress
    private FrameLayout flUploadProgress;
    private TextView tvProgressLabel;

    // Sử dụng StartActivityForResult để mở Gallery truyền thống (ép trỏ đến Thư viện ảnh)
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) uploadAndSend(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) uploadAndSend(uri);
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
        
        // Bấm dấu cộng mở thẳng Thư viện ảnh
        findViewById(R.id.btn_attach).setOnClickListener(v -> openGallery());

        // Initialize progress UI
        flUploadProgress = findViewById(R.id.fl_upload_progress);
        tvProgressLabel = findViewById(R.id.tv_progress_label);

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

    private void openGallery() {
        // Mở thẳng Thư viện ảnh mặc định của thiết bị thay vì trình duyệt tệp
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showProgress(String label) {
        if (flUploadProgress != null) {
            tvProgressLabel.setText(label);
            flUploadProgress.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (flUploadProgress != null) {
            flUploadProgress.setVisibility(View.GONE);
        }
    }

    private void uploadAndSend(Uri uri) {
        String name = "attachment";
        long size = 0;
        String mimeType = getContentResolver().getType(uri);
        boolean isImage = mimeType != null && mimeType.startsWith("image/");

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx != -1) name = cursor.getString(nameIdx);
                if (sizeIdx != -1) size = cursor.getLong(sizeIdx);
            }
        } catch (Exception ignored) {}

        final String finalName = name;
        final long finalSize = size;
        final String msgType = isImage ? RealtimeChatMessage.TYPE_IMAGE : RealtimeChatMessage.TYPE_FILE;

        showProgress(isImage ? "Đang gửi ảnh..." : "Đang gửi tệp...");

        // 1. Tạo tên file an toàn (UUID để tránh trùng lặp)
        String safeFileName = UUID.randomUUID().toString();
        String folder = isImage ? "chat_images/" : "chat_files/";

        // 2. Tạo Storage Reference
        StorageReference ref = FirebaseManager.getStorageReference(folder + safeFileName);

        // 3. Sử dụng putFile thay vì putStream để đảm bảo tính ổn định và metadata
        ref.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                if (task.getException() != null) throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            hideProgress();
            if (task.isSuccessful()) {
                String url = task.getResult().toString();
                // Gửi tin nhắn kèm URL này vào DB
                chatRepository.sendDirectMessage(chatId, currentUserId, currentUserName, "[Hình ảnh]", msgType,
                        isImage ? url : null, !isImage ? url : null, finalName, finalSize,
                        new ChatRepository.OnCompleteListener() {
                            @Override public void onSuccess() {
                                Log.d("ChatApp", "Đã lưu URL ảnh vào DB: " + url);
                            }
                            @Override public void onFailure(String error) {
                                Toast.makeText(DMChatActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Log.e("UploadError", "Failed: ", task.getException());
                Toast.makeText(DMChatActivity.this, "Lỗi upload: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
            }
        });
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
            String sId = r.getSenderId() != null ? r.getSenderId() : "";
            boolean isFirst = !Objects.equals(sId, prevSender);
            Message m = new Message(r.getId(), sId, r.getSenderName(), 0, r.getContent(),
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(r.getCreatedAt())),
                sId.equals(currentUserId), isFirst);
            m.setMessageType(r.getMessageType());
            m.setImageUrl(r.getImageUrl());
            m.setFileUrl(r.getFileUrl());
            m.setFileName(r.getFileName());
            m.setFileSize(r.getFileSize());
            ui.add(m);
            prevSender = sId;
        }
        return ui;
    }

    private void resolveCurrentUserName(String uid) {
        FirebaseManager.getUsersRef().child(uid).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) { if (s.exists()) currentUserName = s.getValue(String.class); }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    private void showAttachOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_attach_options, null);
        
        // Nút Tệp tin bên phải
        view.findViewById(R.id.btn_pick_file).setOnClickListener(v -> {
            dialog.dismiss();
            fileLauncher.launch("*/*");
        });

        // Khu vực lưới ảnh: Mở Thư viện ảnh
        view.findViewById(R.id.rv_photo_picker).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void startPrivateCall(boolean isVideo) {
        String myUid = AuthManager.getInstance(this).getUid();
        if (myUid != null && friendUid != null) {
            Intent intent = new Intent(this, PrivateCallActivity.class);
            intent.putExtra(EXTRA_FRIEND_NAME, friendName);
            intent.putExtra("is_video", isVideo);
            intent.putExtra(EXTRA_FRIEND_UID, friendUid);
            intent.putExtra("is_caller", true);
            startActivity(intent);
        }
    }

    private void showThemePicker() { /* Theme picker logic */ }

    @Override public void onMessageLongPressed(Message m) {}
    @Override public void onReactionChipClicked(Message m, String e) {}

    @Override protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatRepository.removeDirectMessagesObserver(chatId, chatListener);
    }
}
