package com.example.chat_app_frontend.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ServerProfileActivity extends AppCompatActivity {

    private ImageView ivServerAvatar;
    private LinearLayout btnEditAvatar;
    private FrameLayout flAvatarContainer;
    private TextView tvServerName;

    private String serverId;

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

        ivServerAvatar = findViewById(R.id.iv_server_avatar);
        btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        flAvatarContainer = findViewById(R.id.fl_avatar_container);
        tvServerName = findViewById(R.id.tv_profile_server_name);

        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        }
        if (flAvatarContainer != null) {
            flAvatarContainer.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        if (serverId != null && !serverId.isEmpty()) {
            loadServerData();
        } else {
            Toast.makeText(this, "Cảnh báo: Chưa nhận được Server ID để test Backend", Toast.LENGTH_LONG).show();
        }
    }

    private void loadServerData() {
        DatabaseReference serverRef = FirebaseDatabase.getInstance().getReference("servers").child(serverId);
        serverRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String iconUrl = snapshot.child("iconUrl").getValue(String.class);

                if (name != null) tvServerName.setText(name);
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    Glide.with(this).load(iconUrl).centerCrop().into(ivServerAvatar);
                }
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (serverId == null || serverId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có Server ID để lưu ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải ảnh lên máy chủ...", Toast.LENGTH_LONG).show();

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("server_avatars/" + serverId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("servers").child(serverId).child("iconUrl");
                        dbRef.setValue(downloadUrl).addOnSuccessListener(aVoid -> {
                            Toast.makeText(ServerProfileActivity.this, "Lưu ảnh Server thành công!", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ServerProfileActivity.this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}