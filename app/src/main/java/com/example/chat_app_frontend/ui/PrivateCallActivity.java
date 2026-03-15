package com.example.chat_app_frontend.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.VoiceStateManager;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class PrivateCallActivity extends AppCompatActivity {

    private static final String TAG = "PrivateCallActivity";
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private boolean isMicMuted = false;
    private boolean isDeafened = false;
    private boolean isCameraOn = false;
    private boolean isSpeakerOn = true;

    private RtcEngine mRtcEngine;
    private String channelName = "private_call_test";
    private FrameLayout flVideoContainer;
    private View llRemoteInfo;
    private View vRemoteSpeakingBorder;
    private View vLocalSpeakingBorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_private_call);

        String friendName = getIntent().getStringExtra("friend_name");
        if (friendName == null) friendName = "Friend";
        // Use a consistent channel name for testing or separate based on friend
        channelName = "call_" + friendName.toLowerCase().replaceAll("\\s+", "_");

        android.widget.TextView tvTitle = findViewById(R.id.tv_call_title);
        tvTitle.setText(friendName + " >");
        tvTitle.setOnClickListener(v -> {
            CallDetailsBottomSheet bottomSheet = new CallDetailsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "CallDetailsBottomSheet");
        });

        android.widget.TextView tvRemoteName = findViewById(R.id.tv_remote_name);
        if (tvRemoteName != null) tvRemoteName.setText(friendName);

        ImageButton btnCollapse = findViewById(R.id.btn_collapse);
        btnCollapse.setOnClickListener(v -> finish());

        flVideoContainer = findViewById(R.id.fl_video_container);
        llRemoteInfo = findViewById(R.id.ll_remote_info);
        vRemoteSpeakingBorder = findViewById(R.id.v_remote_speaking_border);
        vLocalSpeakingBorder = findViewById(R.id.v_local_speaking_border);

        ImageButton btnCamera = findViewById(R.id.btn_camera);
        ImageButton btnMic = findViewById(R.id.btn_mic);
        ImageButton btnMessage = findViewById(R.id.btn_message);
        ImageButton btnDeafen = findViewById(R.id.btn_deafen);
        ImageButton btnEndCall = findViewById(R.id.btn_end_call);
        ImageButton btnSpeaker = findViewById(R.id.btn_speaker);

        // Handle initial camera state from intent
        isCameraOn = getIntent().getBooleanExtra("is_video", false);
        updateCameraUi(btnCamera);

        btnCamera.setOnClickListener(v -> {
            isCameraOn = !isCameraOn;
            updateCameraUi(btnCamera);
            if (mRtcEngine != null) {
                if (isCameraOn) {
                    mRtcEngine.enableLocalVideo(true);
                    setupLocalVideo();
                    mRtcEngine.startPreview();
                    mRtcEngine.muteLocalVideoStream(false);
                } else {
                    mRtcEngine.stopPreview();
                    mRtcEngine.muteLocalVideoStream(true);
                    mRtcEngine.enableLocalVideo(false);
                    if (flVideoContainer != null) flVideoContainer.removeAllViews();
                    if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.VISIBLE);
                }
                VoiceStateManager.getInstance().setVideoOn(isCameraOn);
            }
        });

        btnMic.setOnClickListener(v -> {
            isMicMuted = !isMicMuted;
            if (mRtcEngine != null) {
                mRtcEngine.muteLocalAudioStream(isMicMuted);
            }
            VoiceStateManager.getInstance().setMuted(isMicMuted);
            if (isMicMuted) {
                btnMic.setImageResource(R.drawable.bg_mic_muted);
            } else {
                btnMic.setImageResource(R.drawable.ic_mic_on);
                if (isDeafened) {
                    isDeafened = false;
                    btnDeafen.setImageResource(R.drawable.ic_speaker);
                    btnDeafen.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(false);
                }
            }
        });

        btnMessage.setOnClickListener(v -> {
            // Logic for message button if needed
        });

        btnDeafen.setOnClickListener(v -> {
            isDeafened = !isDeafened;
            if (isDeafened) {
                btnDeafen.setImageResource(R.drawable.bg_deafen_muted);
                btnDeafen.setImageTintList(null);
                if (!isMicMuted) {
                    isMicMuted = true;
                    btnMic.setImageResource(R.drawable.bg_mic_muted);
                    if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(true);
                }
            } else {
                btnDeafen.setImageResource(R.drawable.ic_speaker);
                btnDeafen.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
        });

        btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            if (mRtcEngine != null) {
                mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
            }
            btnSpeaker.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
        });

        btnEndCall.setOnClickListener(v -> {
            leaveChannel();
        });

        if (checkPermissions()) {
            initializeAndJoinChannel();
        }

        // Sync initial state to VoiceStateManager
        VoiceStateManager stateManager = VoiceStateManager.getInstance();
        stateManager.setConnectedChannel(friendName);
        stateManager.setMuted(isMicMuted);
        stateManager.setVideoOn(isCameraOn);
    }

    private void updateCameraUi(ImageButton btnCamera) {
        if (isCameraOn) {
            btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnCamera.setImageResource(R.drawable.ic_cam_on);
            btnCamera.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        } else {
            btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnCamera.setImageResource(R.drawable.ic_cam_off);
            btnCamera.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAndJoinChannel();
            } else {
                Toast.makeText(this, "Cần quyền truy cập để gọi điện!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeAndJoinChannel() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = getString(R.string.agora_app_id);
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);

            mRtcEngine.enableVideo();
            if (isCameraOn) {
                mRtcEngine.enableLocalVideo(true);
                setupLocalVideo();
                mRtcEngine.startPreview();
            }

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.autoSubscribeAudio = true;
            options.autoSubscribeVideo = true;
            options.publishMicrophoneTrack = true;
            options.publishCameraTrack = isCameraOn;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            mRtcEngine.joinChannel(null, channelName, 0, options);
            mRtcEngine.muteLocalAudioStream(isMicMuted);
            mRtcEngine.muteLocalVideoStream(!isCameraOn);

            // Enable volume indication (interval: 200ms, smooth: 3, report_vad: true)
            mRtcEngine.enableAudioVolumeIndication(200, 3, true);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo Agora: " + e.getMessage());
        }
    }

    private void setupLocalVideo() {
        if (flVideoContainer == null) return;
        TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
        flVideoContainer.removeAllViews();
        flVideoContainer.addView(textureView, new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.GONE);
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, "Đã vào kênh: " + channel);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
        }

        @Override
        public void onUserOffline(int uid, int reason) {
        }

        @Override
        public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
            runOnUiThread(() -> {
                boolean localSpeaking = false;
                boolean remoteSpeaking = false;

                for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                    if (info.volume > 5) { // Threshold for speaking
                        if (info.uid == 0) {
                            localSpeaking = true;
                        } else {
                            remoteSpeaking = true;
                        }
                    }
                }

                if (vLocalSpeakingBorder != null) {
                    vLocalSpeakingBorder.setVisibility(localSpeaking ? View.VISIBLE : View.GONE);
                }
                if (vRemoteSpeakingBorder != null) {
                    vRemoteSpeakingBorder.setVisibility(remoteSpeaking ? View.VISIBLE : View.GONE);
                }

                // Also update global state
                VoiceStateManager.getInstance().setSpeaking(localSpeaking);
            });
        }
    };

    private void leaveChannel() {
        VoiceStateManager.getInstance().leaveChannel();
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