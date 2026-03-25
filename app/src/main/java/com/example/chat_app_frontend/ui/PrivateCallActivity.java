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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.CallSession;
import com.example.chat_app_frontend.repository.CallRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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
    private boolean isCameraOn = false;
    private boolean isSpeakerOn = true;

    private RtcEngine mRtcEngine;
    private String channelName = "private_call_test";
    private String callId;
    private String myUid;
    private String friendUid;
    private boolean isCaller = true;
    /** Đã gọi join (tránh khởi tạo engine 2 lần). */
    private boolean joinStarted = false;
    /** Đã join kênh thành công — bắt buộc true trước khi updateChannelMediaOptions. */
    private volatile boolean rtcInChannel = false;
    private int localRtcUid = 0;
    private int remoteRtcUid = -1;

    // View declarations (Updated to match activity_private_call.xml)
    private TextView tvCallTitle, tvRemoteName;
    private FrameLayout flVideoContainer;
    private FrameLayout flRemoteVideo;
    private FrameLayout flLocalVideoOverlay;
    private CardView cardRemoteVideo;
    private View vRemoteSpeakingBorder, vLocalSpeakingBorder;
    private ImageButton btnCamera, btnMic, btnMessage, btnDeafen, btnEndCall, btnCollapse, btnSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_private_call);

        // 1. Initialize Views
        initViews();

        // 2. Setup Data
        String friendName = getIntent().getStringExtra("friend_name");
        if (friendName == null) friendName = "Friend";
        friendUid = getIntent().getStringExtra(DMChatActivity.EXTRA_FRIEND_UID);
        isCaller = getIntent().getBooleanExtra("is_caller", true);
        myUid = AuthManager.getInstance(this).getUid();

        if (myUid != null && friendUid != null && !friendUid.trim().isEmpty()) {
            callId = CallRepository.buildCallId(myUid, friendUid);
            channelName = CallRepository.buildChannelName(callId);
        } else {
            channelName = "call_" + friendName.toLowerCase().replaceAll("\\s+", "_");
            callId = "mock_" + channelName;
        }

        // 3. Set UI Texts
        tvCallTitle.setText(friendName + " >");
        tvRemoteName.setText(friendName);

        // 4. Handle Button Clicks
        btnCollapse.setOnClickListener(v -> finish());

        btnEndCall.setOnClickListener(v -> leaveChannel());

        // Xử lý bật/tắt Video
        isCameraOn = getIntent().getBooleanExtra("is_video", false);
        updateCameraUi();

        btnCamera.setOnClickListener(v -> {
            isCameraOn = !isCameraOn;
            updateCameraUi();
            if (mRtcEngine != null) {
                if (isCameraOn) {
                    mRtcEngine.enableLocalVideo(true);
                    setupLocalVideo();
                    mRtcEngine.startPreview();
                    mRtcEngine.muteLocalVideoStream(false);
                    applyCameraPublishToChannel(true);
                } else {
                    applyCameraPublishToChannel(false);
                    mRtcEngine.stopPreview();
                    mRtcEngine.muteLocalVideoStream(true);
                    mRtcEngine.enableLocalVideo(false);
                    if (flLocalVideoOverlay != null) {
                        flLocalVideoOverlay.removeAllViews();
                        flLocalVideoOverlay.setVisibility(View.GONE);
                    }
                    View llRemoteInfo = findViewById(R.id.ll_remote_info);
                    if (remoteRtcUid < 0 && flRemoteVideo != null) {
                        flRemoteVideo.removeAllViews();
                        flRemoteVideo.setVisibility(View.GONE);
                        if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.VISIBLE);
                    }
                }
                VoiceStateManager.getInstance().setVideoOn(isCameraOn);
            }
        });

        // Xử lý Mute Mic
        btnMic.setOnClickListener(v -> {
            isMicMuted = !isMicMuted;
            updateMicUi();
            if (mRtcEngine != null) {
                mRtcEngine.muteLocalAudioStream(isMicMuted);
            }
            VoiceStateManager.getInstance().setMuted(isMicMuted);
        });

        // Loa ngoài
        btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            if (mRtcEngine != null) {
                mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
            }
            btnSpeaker.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
        });

        // Khởi động
        if (checkPermissions()) {
            if (!isCaller) {
                CallRepository.getInstance().updateStatus(callId, "accepted");
            }
            initializeAndJoinChannel();
        }

        observeCallSession();

        // Sync state
        VoiceStateManager stateManager = VoiceStateManager.getInstance();
        stateManager.setConnectedChannel(friendName);
        stateManager.setMuted(isMicMuted);
        stateManager.setVideoOn(isCameraOn);
    }

    private void initViews() {
        btnCollapse = findViewById(R.id.btn_collapse);
        tvCallTitle = findViewById(R.id.tv_call_title);
        btnSpeaker = findViewById(R.id.btn_speaker);

        cardRemoteVideo = findViewById(R.id.card_remote_video);
        flVideoContainer = findViewById(R.id.fl_video_container);
        flRemoteVideo = findViewById(R.id.fl_remote_video);
        flLocalVideoOverlay = findViewById(R.id.fl_local_video_overlay);
        tvRemoteName = findViewById(R.id.tv_remote_name);
        vRemoteSpeakingBorder = findViewById(R.id.v_remote_speaking_border);
        vLocalSpeakingBorder = findViewById(R.id.v_local_speaking_border);

        btnCamera = findViewById(R.id.btn_camera);
        btnMic = findViewById(R.id.btn_mic);
        btnMessage = findViewById(R.id.btn_message);
        btnDeafen = findViewById(R.id.btn_deafen);
        btnEndCall = findViewById(R.id.btn_end_call);
    }

    private void updateCameraUi() {
        if (isCameraOn) {
            btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnCamera.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            btnCamera.setImageResource(R.drawable.ic_camera); // Giả sử ic_camera là icon cam on
        } else {
            btnCamera.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnCamera.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            btnCamera.setImageResource(R.drawable.ic_cam_off);
        }
    }

    private void updateMicUi() {
        if (isMicMuted) {
            btnMic.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnMic.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ED4245")));
            btnMic.setImageResource(R.drawable.ic_mic_off);
        } else {
            btnMic.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnMic.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            btnMic.setImageResource(R.drawable.ic_mic_on);
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
                if (!isCaller && callId != null) {
                    CallRepository.getInstance().updateStatus(callId, "accepted");
                }
                initializeAndJoinChannel();
            } else {
                Toast.makeText(this, "Cần quyền truy cập để gọi điện!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeAndJoinChannel() {
        if (joinStarted) return;
        joinStarted = true;
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
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            options.enableAudioRecordingOrPlayout = true;
            options.autoSubscribeAudio = true;
            options.autoSubscribeVideo = true;
            options.publishMicrophoneTrack = true;
            options.publishCameraTrack = isCameraOn;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            mRtcEngine.joinChannel(null, channelName, 0, options);
            mRtcEngine.muteLocalAudioStream(isMicMuted);
            mRtcEngine.muteLocalVideoStream(!isCameraOn);

            mRtcEngine.enableAudioVolumeIndication(200, 3, true);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo Agora: " + e.getMessage());
        }
    }

    /**
     * Agora 4.x: nếu join với publishCameraTrack = false, bật camera sau đó phải
     * {@link RtcEngine#updateChannelMediaOptions(ChannelMediaOptions)} thì phía bên kia mới nhận được video.
     */
    private void applyCameraPublishToChannel(boolean publishing) {
        if (mRtcEngine == null || !rtcInChannel) {
            return;
        }
        ChannelMediaOptions opts = new ChannelMediaOptions();
        opts.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        opts.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        opts.enableAudioRecordingOrPlayout = true;
        opts.publishMicrophoneTrack = true;
        opts.publishCameraTrack = publishing;
        opts.autoSubscribeAudio = true;
        opts.autoSubscribeVideo = true;
        int code = mRtcEngine.updateChannelMediaOptions(opts);
        if (code != 0) {
            Log.w(TAG, "updateChannelMediaOptions publishCameraTrack=" + publishing + " code=" + code);
        }
    }

    private void setupLocalVideo() {
        if (mRtcEngine == null || flRemoteVideo == null) return;
        View llRemoteInfo = findViewById(R.id.ll_remote_info);
        boolean remoteHasVideo = flRemoteVideo.getVisibility() == View.VISIBLE && remoteRtcUid >= 0;
        FrameLayout host = remoteHasVideo && flLocalVideoOverlay != null ? flLocalVideoOverlay : flRemoteVideo;

        if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.GONE);

        host.removeAllViews();
        host.setVisibility(View.VISIBLE);

        TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
        host.addView(textureView, new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        if (mRtcEngine == null || flRemoteVideo == null || uid <= 0) return;
        if (localRtcUid != 0 && uid == localRtcUid) return;
        remoteRtcUid = uid;
        View llRemoteInfo = findViewById(R.id.ll_remote_info);
        if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.GONE);

        flRemoteVideo.removeAllViews();
        flRemoteVideo.setVisibility(View.VISIBLE);

        TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
        flRemoteVideo.addView(textureView, new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupRemoteVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

        if (isCameraOn) {
            if (flLocalVideoOverlay != null) flLocalVideoOverlay.setVisibility(View.VISIBLE);
            setupLocalVideo();
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.d(TAG, "onUserJoined uid=" + uid + " localRtcUid=" + localRtcUid);
            if (localRtcUid != 0 && uid == localRtcUid) return;
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            if (uid <= 0 || (localRtcUid != 0 && uid == localRtcUid)) return;
            if (state != Constants.REMOTE_VIDEO_STATE_DECODING && state != Constants.REMOTE_VIDEO_STATE_STARTING) {
                return;
            }
            Log.d(TAG, "onRemoteVideoStateChanged uid=" + uid + " state=" + state);
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                if (uid == remoteRtcUid) {
                    remoteRtcUid = -1;
                    if (flRemoteVideo != null) {
                        flRemoteVideo.removeAllViews();
                        flRemoteVideo.setVisibility(View.GONE);
                    }
                    View llRemoteInfo = findViewById(R.id.ll_remote_info);
                    if (llRemoteInfo != null) llRemoteInfo.setVisibility(View.VISIBLE);
                    if (isCameraOn) {
                        if (flLocalVideoOverlay != null) {
                            flLocalVideoOverlay.removeAllViews();
                            flLocalVideoOverlay.setVisibility(View.GONE);
                        }
                        setupLocalVideo();
                    }
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, "onJoinChannelSuccess channel=" + channel + " uid=" + uid);
            localRtcUid = uid;
            rtcInChannel = true;
            runOnUiThread(() -> {
                applyCameraPublishToChannel(isCameraOn);
            });
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            rtcInChannel = false;
            localRtcUid = 0;
        }

        @Override
        public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
            runOnUiThread(() -> {
                boolean isLocalSpeakingNow = false;
                boolean isRemoteSpeakingNow = false;

                if (speakers != null) {
                    for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                        if (info.volume <= 5) continue;
                        if (info.uid == 0) isLocalSpeakingNow = true;
                        else isRemoteSpeakingNow = true;
                    }
                }
                isLocalSpeakingNow = isLocalSpeakingNow && !isMicMuted;

                if (vLocalSpeakingBorder != null) {
                    vLocalSpeakingBorder.setVisibility(isLocalSpeakingNow ? View.VISIBLE : View.GONE);
                }
                if (vRemoteSpeakingBorder != null) {
                    vRemoteSpeakingBorder.setVisibility(isRemoteSpeakingNow ? View.VISIBLE : View.GONE);
                }

                VoiceStateManager.getInstance().setSpeaking(isLocalSpeakingNow);
            });
        }
    };

    private void leaveChannel() {
        VoiceStateManager.getInstance().leaveChannel();
        if (callId != null) {
            CallRepository.getInstance().updateStatus(callId, "ended");
        }
        rtcInChannel = false;
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
        finish();
    }

    private ValueEventListener callSessionListener;

    private void observeCallSession() {
        if (callId == null) return;
        callSessionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                CallSession session = snapshot.getValue(CallSession.class);
                if (session == null || session.getStatus() == null) return;

                String status = session.getStatus();
                if ("ended".equals(status) || "rejected".equals(status)) {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(PrivateCallActivity.this, "Cuộc gọi đã kết thúc", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "observeCallSession cancelled: " + error.getMessage());
            }
        };
        CallRepository.getInstance().observeSession(callId, callSessionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcInChannel = false;
        if (callSessionListener != null && callId != null) {
            CallRepository.getInstance().removeObserver(callId, callSessionListener);
            callSessionListener = null;
        }
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
