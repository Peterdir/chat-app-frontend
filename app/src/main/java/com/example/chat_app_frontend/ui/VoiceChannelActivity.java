package com.example.chat_app_frontend.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Rational;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class VoiceChannelActivity extends AppCompatActivity {
    private static final String TAG = "VoiceChannelActivity";
    private static final int PERMISSION_REQ_ID = 22;

    // Các biến giao diện MỚI
    private TextView tvTopChannelName;
    private ImageButton btnCollapse;
    private ImageButton btnAddUserTop;
    private ImageButton btnSpeakerTop;

    // Nút ở dưới cùng
    private ImageButton btnVideo;
    private ImageButton btnMuteMain;
    private ImageButton btnChatMain;
    private ImageButton btnEventMain;
    private ImageButton btnLeaveMain;

    // Pill trong Avatar
    private TextView tvUserNameStatus;

    // Trạng thái nút bấm
    private boolean isMuted = true; // Mặc định vào kênh là TẮT mic theo chuẩn Discord
    private boolean isSpeakerOn = true;
    private boolean isVideoOn = false; // Mặc định tắt camera lúc vào phòng

    private FrameLayout flLocalVideo;
    private CardView cardMainAvatar;

    // Biến cấu hình phòng gọi
    private String channelName = "test_channel";
    private RtcEngine mRtcEngine;
    private String appId;

    // Danh sách quyền cần xin
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_channel);

        // Lấy tên kênh được truyền từ BottomSheet/Adapter
        if (getIntent() != null) {
            if (getIntent().hasExtra("CHANNEL_NAME")) {
                channelName = getIntent().getStringExtra("CHANNEL_NAME");
            }
            if (getIntent().hasExtra("IS_MUTED")) {
                isMuted = getIntent().getBooleanExtra("IS_MUTED", true);
            }
        }

        // Tìm view theo ID mới
        tvTopChannelName = findViewById(R.id.tv_top_channel_name);
        btnCollapse = findViewById(R.id.btn_collapse);
        cardMainAvatar = findViewById(R.id.card_main_avatar);
        btnAddUserTop = findViewById(R.id.btn_add_user_top);
        btnSpeakerTop = findViewById(R.id.btn_speaker_top);

        btnVideo = findViewById(R.id.btn_video);
        btnMuteMain = findViewById(R.id.btn_mute_main);
        btnChatMain = findViewById(R.id.btn_chat_main);
        btnEventMain = findViewById(R.id.btn_event_main);
        btnLeaveMain = findViewById(R.id.btn_leave_main);

        tvUserNameStatus = findViewById(R.id.tv_user_name_status);

        tvTopChannelName.setText(channelName);

        flLocalVideo = findViewById(R.id.fl_local_video);

        // Lấy App ID từ file strings.xml
        appId = getString(R.string.agora_app_id);

        // Khởi tạo trạng thái nút Mic UI ban đầu
        updateMicUiState();
        updateVideoUiState();

        setupClickListeners();

        // Xin quyền thu âm rôi mới khởi tạo SDK
        if (checkPermissions()) {
            initializeAndJoinChannel();
        }
    }

    private void animateClick(android.view.View view) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100);
        }).start();
    }

    private void updateVideoUiState() {
        if (!isVideoOn) {
            btnVideo.setImageResource(R.drawable.ic_videocam_off);
            btnVideo.setBackgroundResource(R.drawable.bg_circle_dark);
            btnVideo.setColorFilter(ContextCompat.getColor(this, R.color.white));
        } else {
            btnVideo.setImageResource(R.drawable.ic_videocam);
            btnVideo.setBackgroundResource(R.drawable.bg_circle_white);
            btnVideo.setColorFilter(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private void updateMicUiState() {
        android.widget.ImageView ivUserMicStatus = findViewById(R.id.iv_user_mic_status);
        if (isMuted) {
            // TẮT MIC: Nền Trắng, Icon Đỏ
            btnMuteMain.setImageResource(R.drawable.ic_mic_off);
            btnMuteMain.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnMuteMain.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));

            if (ivUserMicStatus != null) {
                ivUserMicStatus.setVisibility(View.VISIBLE);
                ivUserMicStatus.setImageResource(R.drawable.ic_mic_off);
                ivUserMicStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }
        } else {
            // BẬT MIC: Nền Xám, Icon Trắng
            btnMuteMain.setImageResource(R.drawable.ic_mic); // Hãy chắc chắn file ic_mic bạn làm rồi nha
            btnMuteMain.setColorFilter(ContextCompat.getColor(this, R.color.white));
            btnMuteMain.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

            if (ivUserMicStatus != null) {
                ivUserMicStatus.setVisibility(View.VISIBLE);
                ivUserMicStatus.setImageResource(R.drawable.ic_mic);
                ivUserMicStatus.setColorFilter(android.graphics.Color.parseColor("#43B581")); // Màu Xanh Lá
            }
        }
    }

    private void setupClickListeners() {
        // Nút Gập xuống
        btnCollapse.setOnClickListener(v -> {
            animateClick(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
                pipBuilder.setAspectRatio(new Rational(1, 1)); // Hình vuông giống Discord
                enterPictureInPictureMode(pipBuilder.build());
            } else {
                finish(); // Android cũ thì đành đóng luôn
            }
        });

        // Nút Tắt/Bật Micro (Màu thay đổi ảo diệu)
        btnMuteMain.setOnClickListener(v -> {
            animateClick(v);
            isMuted = !isMuted;
            if (mRtcEngine != null) {
                mRtcEngine.muteLocalAudioStream(isMuted);
            }

            updateMicUiState();
        });

        // Nút Loa
        btnSpeakerTop.setOnClickListener(v -> {
            animateClick(v);
            isSpeakerOn = !isSpeakerOn;
            if (mRtcEngine != null) {
                mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
            }
            btnSpeakerTop.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
        });

        // Cập nhật logic Tắt/Bật Video
        btnVideo.setOnClickListener(v -> {
            animateClick(v);
            isVideoOn = !isVideoOn;
            if (mRtcEngine != null) {
                if (isVideoOn) {
                    mRtcEngine.enableLocalVideo(true); // Bật hardware camera

                    // Sử dụng CreateTextureView thay vì SurfaceView để hỗ trợ CardView và tránh lỗi
                    // màn hình đen
                    TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
                    flLocalVideo.removeAllViews();
                    flLocalVideo.addView(textureView, new FrameLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                    mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
                    mRtcEngine.startPreview();
                    mRtcEngine.muteLocalVideoStream(false);
                    flLocalVideo.setVisibility(View.VISIBLE);
                    findViewById(R.id.iv_center_avatar).setVisibility(View.GONE);
                } else {
                    mRtcEngine.stopPreview();
                    mRtcEngine.muteLocalVideoStream(true);
                    mRtcEngine.enableLocalVideo(false); // Tắt hardware camera
                    flLocalVideo.removeAllViews();
                    flLocalVideo.setVisibility(View.GONE);
                    findViewById(R.id.iv_center_avatar).setVisibility(View.VISIBLE);
                }
            }
            updateVideoUiState();
        });
        btnChatMain.setOnClickListener(v -> {
            animateClick(v);
            Toast.makeText(this, "Mở cửa sổ Chat", Toast.LENGTH_SHORT).show();
        });
        btnEventMain.setOnClickListener(v -> {
            animateClick(v);
            Toast.makeText(this, "Mở Soundboard", Toast.LENGTH_SHORT).show();
        });
        btnAddUserTop.setOnClickListener(v -> {
            animateClick(v);
            Toast.makeText(this, "Thêm người", Toast.LENGTH_SHORT).show();
        });

        btnLeaveMain.setOnClickListener(v -> {
            animateClick(v);
            leaveChannel();
        });
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        View pillChannelName = findViewById(R.id.pill_channel_name);
        View bannerAddPeople = findViewById(R.id.banner_add_people);
        View bottomActionBar = findViewById(R.id.bottom_action_bar);

        if (isInPictureInPictureMode) {
            // Ẩn các thành phần không cần thiết
            btnCollapse.setVisibility(View.GONE);
            pillChannelName.setVisibility(View.GONE);
            btnAddUserTop.setVisibility(View.GONE);
            btnSpeakerTop.setVisibility(View.GONE);
            bannerAddPeople.setVisibility(View.GONE);
            bottomActionBar.setVisibility(View.GONE);
        } else {
            // Hiện lại khi phóng to
            btnCollapse.setVisibility(View.VISIBLE);
            pillChannelName.setVisibility(View.VISIBLE);
            btnAddUserTop.setVisibility(View.VISIBLE);
            btnSpeakerTop.setVisibility(View.VISIBLE);
            bannerAddPeople.setVisibility(View.VISIBLE);
            bottomActionBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Tự động thu nhỏ khi người dùng bấm phím Home ngoài hệ thống nếu muốn trải
        // nghiệm liền mạch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(new Rational(1, 1));
            enterPictureInPictureMode(pipBuilder.build());
        }
    }

    // --- Các hàm xử lý SDK Agora ---
    private boolean checkPermissions() {
        for (String permission : REQUESTED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (grantResults.length > 0 && allGranted) {
                initializeAndJoinChannel();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để tham gia Voice!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeAndJoinChannel() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);

            // Kích hoạt module Video
            mRtcEngine.enableVideo();
            mRtcEngine.enableLocalVideo(isVideoOn); // Khởi tạo hardware tuỳ trạng thái

            // Setup camera ban đầu nếu isVideoOn = true
            if (isVideoOn) {
                TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
                flLocalVideo.removeAllViews();
                flLocalVideo.addView(textureView, new FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
                mRtcEngine.startPreview();
            }

            // Bật theo dõi âm lượng (Kiểm tra mỗi 200ms) báo cáo mic
            mRtcEngine.enableAudioVolumeIndication(200, 3, true);

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.autoSubscribeAudio = true;
            options.publishMicrophoneTrack = true;
            options.publishCameraTrack = true; // Quan trọng để gửi video
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            // Tham gia
            mRtcEngine.joinChannel(null, channelName, 0, options);
            mRtcEngine.muteLocalAudioStream(isMuted);
            mRtcEngine.muteLocalVideoStream(!isVideoOn);
            if (isVideoOn) {
                flLocalVideo.setVisibility(View.VISIBLE);
                findViewById(R.id.iv_center_avatar).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo Agora: " + e.getMessage());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> {
                tvUserNameStatus.setText("Đã Kết Nối");
                tvUserNameStatus.setTextColor(0xFF43B581); // Xanh
                                                           // lá
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                Toast.makeText(VoiceChannelActivity.this, "Có người tham gia", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
            boolean localSpeaking = false;
            if (speakers != null) {
                for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                    if (info.uid == 0 && info.volume > 5) {
                        localSpeaking = true;
                        break;
                    }
                }
            }
            final boolean finalSpeaking = localSpeaking && !isMuted;
            runOnUiThread(() -> {
                if (cardMainAvatar != null) {
                    // Cây đũa phép: Thay vì Stroke (Vốn không Support viền API cũ), ta đổi
                    // Background của Card thành Xanh,
                    // sau đó Layout con bên trong cách vào 4dp là thành "Khung Viền Xanh" - Hack
                    // UI.
                    if (finalSpeaking) {
                        cardMainAvatar.setCardBackgroundColor(android.graphics.Color.parseColor("#43B581"));
                        int padding = (int) (4 * getResources().getDisplayMetrics().density);
                        cardMainAvatar.setContentPadding(padding, padding, padding, padding);
                    } else {
                        cardMainAvatar.setCardBackgroundColor(android.graphics.Color.parseColor("#111214"));
                        cardMainAvatar.setContentPadding(0, 0, 0, 0);
                    }
                }
            });
        }
    };

    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
