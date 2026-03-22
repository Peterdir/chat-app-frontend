package com.example.chat_app_frontend.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ServerProfileActivity extends AppCompatActivity {

    private ImageView ivServerAvatar;
    private LinearLayout btnEditAvatar;
    private FrameLayout flAvatarContainer;
    private TextView tvServerName;

    private TextInputLayout tilServerName;
    private TextInputEditText etServerName;
    private MaterialButton btnSaveServerName;

    private String serverId;
    private String originalServerName = "";

    // Flag để TextWatcher bỏ qua khi ta tự gán text bằng code
    private boolean ignoreTextChange = false;

    // Real-time listener
    private DatabaseReference serverRef;
    private ValueEventListener serverValueListener;
    private boolean isFirstLoad = true;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivServerAvatar.setImageURI(uri);
                    uploadImageToFirebase(uri);
                } else {
                    Toast.makeText(this, "Đã hủy chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_server_profile);

        serverId = getIntent().getStringExtra("SERVER_ID");

        // Ánh xạ view
        ivServerAvatar = findViewById(R.id.iv_server_avatar);
        btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        flAvatarContainer = findViewById(R.id.fl_avatar_container);
        tvServerName = findViewById(R.id.tv_profile_server_name);
        tilServerName = findViewById(R.id.til_server_name);
        etServerName = findViewById(R.id.et_server_name);
        btnSaveServerName = findViewById(R.id.btn_save_server_name);

        // Sự kiện đổi avatar
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        }
        if (flAvatarContainer != null) {
            flAvatarContainer.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        // Toolbar back
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // === NÚT LƯU: mặc định disabled ===
        btnSaveServerName.setEnabled(false);
        btnSaveServerName.setAlpha(0.5f);

        // === SỰ KIỆN CLICK NÚT LƯU ===
        btnSaveServerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = etServerName.getText().toString().trim();

                if (newName.isEmpty()) {
                    tilServerName.setError("Tên Server không được để trống");
                    return;
                }

                if (serverId == null || serverId.isEmpty()) {
                    Toast.makeText(ServerProfileActivity.this, "Lỗi: Không có Server ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Disable nút ngay để tránh bấm 2 lần
                btnSaveServerName.setEnabled(false);
                btnSaveServerName.setAlpha(0.5f);

                // Gọi Firebase cập nhật tên
                DatabaseReference nameRef = FirebaseDatabase.getInstance()
                        .getReference("servers")
                        .child(serverId)
                        .child("name");

                nameRef.setValue(newName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ServerProfileActivity.this, "Đổi tên thành công!", Toast.LENGTH_SHORT).show();
                            originalServerName = newName;
                            btnSaveServerName.setEnabled(false);
                            btnSaveServerName.setAlpha(0.5f);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ServerProfileActivity.this, "Lỗi đổi tên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // Cho phép bấm lại nếu lỗi
                            btnSaveServerName.setEnabled(true);
                            btnSaveServerName.setAlpha(1.0f);
                        });
            }
        });

        // === TEXT WATCHER: enable/disable nút khi user gõ ===
        etServerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Bỏ qua nếu đang set text bằng code (load từ Firebase)
                if (ignoreTextChange) return;

                String current = s.toString().trim();
                boolean shouldEnable = !current.equals(originalServerName) && current.length() > 0;

                btnSaveServerName.setEnabled(shouldEnable);
                btnSaveServerName.setAlpha(shouldEnable ? 1.0f : 0.5f);

                // Xóa lỗi validation khi user gõ
                tilServerName.setError(null);
            }
        });

        // === LOAD DỮ LIỆU SERVER ===
        if (serverId != null && !serverId.isEmpty()) {
            loadServerData();
        } else {
            Toast.makeText(this, "Cảnh báo: Chưa nhận được Server ID để test Backend", Toast.LENGTH_LONG).show();
        }
    }

    private void loadServerData() {
        serverRef = FirebaseDatabase.getInstance().getReference("servers").child(serverId);

        serverValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String iconUrl = snapshot.child("iconUrl").getValue(String.class);

                    // Cập nhật tên trên Toolbar
                    if (name != null) {
                        tvServerName.setText(name);
                    }

                    // Cập nhật avatar
                    if (iconUrl != null && !iconUrl.isEmpty()) {
                        Glide.with(ServerProfileActivity.this)
                                .load(iconUrl)
                                .centerCrop()
                                .into(ivServerAvatar);
                    }

                    // Chỉ set text vào ô nhập khi load lần đầu
                    if (isFirstLoad) {
                        isFirstLoad = false;
                        originalServerName = name != null ? name : "";

                        // BẬT FLAG để TextWatcher bỏ qua lần setText này
                        ignoreTextChange = true;
                        etServerName.setText(originalServerName);
                        ignoreTextChange = false;

                        // Đảm bảo nút disabled sau khi load
                        btnSaveServerName.setEnabled(false);
                        btnSaveServerName.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServerProfileActivity.this,
                        "Lỗi tải dữ liệu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        serverRef.addValueEventListener(serverValueListener);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (serverId == null || serverId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có Server ID để lưu ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải ảnh lên máy chủ...", Toast.LENGTH_LONG).show();

        StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference()
                .child("server_avatars/" + serverId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                .getReference("servers")
                                .child(serverId)
                                .child("iconUrl");
                        dbRef.setValue(downloadUrl).addOnSuccessListener(aVoid -> {
                            Toast.makeText(ServerProfileActivity.this,
                                    "Lưu ảnh Server thành công!",
                                    Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ServerProfileActivity.this,
                            "Lỗi tải ảnh: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverRef != null && serverValueListener != null) {
            serverRef.removeEventListener(serverValueListener);
        }
    }
}