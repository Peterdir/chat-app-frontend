package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private String userId;
    private ValueEventListener profileListener;
    
    private View profileBanner;
    private ImageView imgAvatar, imgDecoration;
    private TextView tvDisplayName, tvUsername, tvBio, tvJoinedDate, tvMutualServers;
    private View btnMessage, btnVoiceCall, btnVideoCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserData();
    }

    private void initViews() {
        profileBanner = findViewById(R.id.profile_banner);
        imgAvatar = findViewById(R.id.img_profile_avatar);
        imgDecoration = findViewById(R.id.img_profile_decoration);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvBio = findViewById(R.id.tv_bio);
        tvJoinedDate = findViewById(R.id.tv_joined_date);
        tvMutualServers = findViewById(R.id.tv_mutual_servers);
        
        btnMessage = findViewById(R.id.btn_message);
        btnVoiceCall = findViewById(R.id.btn_voice_call);
        btnVideoCall = findViewById(R.id.btn_video_call);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        btnMessage.setOnClickListener(v -> {
            // Logic mở chat DM
            Intent intent = new Intent(this, DMChatActivity.class);
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_NAME, tvDisplayName.getText().toString());
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, userId);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        profileListener = UserRepository.getInstance().observeUser(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                updateUI(user);
            }

            @Override
            public void onUserNotFound() {
                Toast.makeText(UserProfileActivity.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(User user) {
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUserName();
        tvDisplayName.setText(displayName);
        tvUsername.setText(user.getUserName());
        
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
        }

        ProfileUIUtils.loadUserProfile(this, user, imgAvatar, imgDecoration, null, null);
        
        // Cập nhật Banner nếu có
        // if (user.getBannerColor() != null) profileBanner.setBackgroundColor(Color.parseColor(user.getBannerColor()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userId != null && profileListener != null) {
            UserRepository.getInstance().removeListener(userId, profileListener);
        }
    }
}
