package com.example.chat_app_frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Rational;
import android.view.View;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.service.ScreenShareForegroundService;
import com.example.chat_app_frontend.utils.AgoraScreenShareController;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VoiceChannelActivity extends AppCompatActivity implements VoiceStateManager.VoiceStateListener {
    private static final String TAG = "VoiceChannelActivity";
    private static final int PERMISSION_REQ_ID = 22;
    private static final int NOTIFICATION_PERMISSION_REQ_ID = 23;
    private static final int CAMERA_PERMISSION_REQ_ID = 24;

    // View declarations
    private TextView tvTopChannelName, tvParticipantCount, tvUserNameStatus;
    private ImageButton btnCollapse, btnAddUserTop, btnSpeakerTop;
    private ImageButton btnVideo, btnMuteMain, btnChatMain, btnEventMain, btnLeaveMain, btnSwitchCamera;

    private FrameLayout flCenterAvatarContainer;
    private FrameLayout flScreenSharePreview;
    private GridLayout glVideoGrid;
    private View vMainSpeakingBorder;
    private ImageView ivCenterAvatar;
    private ImageView ivScreenShareIllustration;
    private TextView tvShareLabel;

    private ValueEventListener userProfileListener;

    // Call Config
    private String channelName = "test_channel";
    private RtcEngine mRtcEngine;
    private String appId;
    private boolean hasJoinedChannel = false;

    // Status
    private boolean isMuted = true;
    private boolean isSpeakerOn = true;
    private boolean isVideoOn = false;
    private boolean isLocalSpeaking = false;
    private boolean isScreenSharing = false;
    private boolean isScreenShareStarting = false;
    private boolean pendingScreenShareAfterJoin = false;

    private View cvScreenShareStatus;
    private android.widget.Button btnStopScreenShareMain;
    private MediaProjectionManager mediaProjectionManager;
    private ActivityResultLauncher<Intent> screenSharePermissionLauncher;
    @Nullable
    private Intent pendingScreenShareData;
    private int pendingScreenShareResultCode = RESULT_CANCELED;
    private AgoraScreenShareController screenShareController;
    @Nullable
    private Integer renderedScreenShareUid;
    private boolean renderedScreenShareIsLocal = false;

    // User tracking
    private int localUid = 0; 
    private final Set<Integer> remoteUids = new HashSet<>();
    private final Set<Integer> remoteVideoUids = new HashSet<>();
    private final Set<Integer> remoteScreenShareUids = new HashSet<>();
    private final Set<Integer> speakingUids = new HashSet<>();
    private final Set<Integer> remoteAudioMutedUids = new HashSet<>();
    private final Map<Integer, String> agoraUidToFirebaseUid = new HashMap<>();
    private final Map<Integer, String> agoraUidToDisplayLabel = new HashMap<>();

    private DatabaseReference voiceAgoraMapRef;
    private ValueEventListener voiceAgoraMapListener;
    private DatabaseReference voiceParticipantStateRef;
    private ValueEventListener voiceParticipantStateListener;
    private String localDisplayName = "Ban";

    // UI mapping for speaking borders in grid
    private final Map<String, View> tileSpeakingBorders = new HashMap<>();

    private static final String[] REQUIRED_CALL_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_voice_channel);

        // Get Intent Data
        if (getIntent() != null) {
            channelName = getIntent().getStringExtra("CHANNEL_NAME") != null ? getIntent().getStringExtra("CHANNEL_NAME") : channelName;
            isMuted = getIntent().getBooleanExtra("IS_MUTED", true);
        }
        appId = getString(R.string.agora_app_id);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        screenSharePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        isScreenShareStarting = false;
                        clearPendingScreenSharePermission();
                        Toast.makeText(this, "Ban da huy chia se man hinh", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pendingScreenShareResultCode = result.getResultCode();
                    pendingScreenShareData = result.getData();
                    startScreenShareForegroundService();
                }
        );

        initViews();

        tvTopChannelName.setText(channelName);
        updateParticipantCountUi();
        updateMicUiState();
        updateVideoUiState();

        // Sync initial state to Manager
        VoiceStateManager stateManager = VoiceStateManager.getInstance();
        stateManager.setConnectedChannel(channelName);
        stateManager.setMuted(isMuted);
        stateManager.setVideoOn(isVideoOn);
        stateManager.setCurrentActivityStatus("đang chill");

        setupClickListeners();

        startVoiceAgoraUidMapListener();
        startVoiceParticipantStateListener();

        ensureVoiceChannelReady(false);
        requestNotificationPermissionIfNeeded();

        VoiceStateManager.getInstance().addListener(this);
    }

    @Override
    public void onVoiceStateChanged() {
        VoiceStateManager sm = VoiceStateManager.getInstance();
        if (sm.getConnectedChannelName() == null) {
            // User disconnected from global bar
            if (!isFinishing() && mRtcEngine != null) {
                mRtcEngine.leaveChannel();
                RtcEngine.destroy();
                mRtcEngine = null;
                finish();
            }
        } else {
            // Check mute state
            if (isMuted != sm.isMuted()) {
                isMuted = sm.isMuted();
                if (mRtcEngine != null) {
                    mRtcEngine.muteLocalAudioStream(isMuted);
                }
                updateMicUiState();
            }
        }
    }

    private void initViews() {
        tvTopChannelName = findViewById(R.id.tv_top_channel_name);
        tvParticipantCount = findViewById(R.id.tv_participant_count);
        tvUserNameStatus = findViewById(R.id.tv_user_name_status);

        btnCollapse = findViewById(R.id.btn_collapse);
        btnAddUserTop = findViewById(R.id.btn_add_user_top);
        btnSpeakerTop = findViewById(R.id.btn_speaker_top);

        btnVideo = findViewById(R.id.btn_video_sheet);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera_top);
        btnMuteMain = findViewById(R.id.btn_mute_sheet);
        btnChatMain = findViewById(R.id.btn_chat_sheet);
        btnEventMain = findViewById(R.id.btn_soundboard_sheet);
        btnLeaveMain = findViewById(R.id.btn_leave_sheet);

        flCenterAvatarContainer = findViewById(R.id.fl_center_avatar_container);
        flScreenSharePreview = findViewById(R.id.fl_screen_share_preview);
        glVideoGrid = findViewById(R.id.gl_video_grid);
        vMainSpeakingBorder = findViewById(R.id.v_main_speaking_border);

        ivCenterAvatar = findViewById(R.id.iv_center_avatar);
        cvScreenShareStatus = findViewById(R.id.cv_screen_share_status);
        btnStopScreenShareMain = findViewById(R.id.btn_stop_screen_share_main);
        ivScreenShareIllustration = findViewById(R.id.iv_screen_share_illustration);
        tvShareLabel = findViewById(R.id.tv_share_label);
        if (ivScreenShareIllustration != null) {
            ivScreenShareIllustration.setImageResource(R.drawable.illustration_screen_share);
        }
    }

    private void animateClick(View view) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100);
        }).start();
    }

    private void updateVideoUiState() {
        if (isVideoOn) {
            btnVideo.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnVideo.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            btnVideo.setImageResource(R.drawable.ic_cam_on);
        } else {
            btnVideo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnVideo.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            btnVideo.setImageResource(R.drawable.ic_cam_off);
        }
        if (btnSwitchCamera != null) {
            btnSwitchCamera.setVisibility(isVideoOn ? View.VISIBLE : View.GONE);
        }
    }

    private void updateMicUiState() {
        if (isMuted) {
            btnMuteMain.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnMuteMain.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ED4245")));
            btnMuteMain.setImageResource(R.drawable.ic_mic_off);

            ImageView ivUserMicStatus = findViewById(R.id.iv_user_mic_status);
            if (ivUserMicStatus != null) {
                ivUserMicStatus.setImageResource(R.drawable.ic_mic_off);
                ivUserMicStatus.setColorFilter(Color.parseColor("#ED4245"));
            }
        } else {
            btnMuteMain.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnMuteMain.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            btnMuteMain.setImageResource(R.drawable.ic_mic_on);

            ImageView ivUserMicStatus = findViewById(R.id.iv_user_mic_status);
            if (ivUserMicStatus != null) {
                ivUserMicStatus.setImageResource(R.drawable.ic_mic_on);
                ivUserMicStatus.setColorFilter(Color.parseColor("#43B581"));
            }
        }
        
        // Sync with sheet switch if it exists
        androidx.appcompat.widget.SwitchCompat switchMuteAll = findViewById(R.id.switch_mute_all);
        if (switchMuteAll != null) {
            switchMuteAll.setChecked(isMuted);
        }

        startObservingUserProfile();
    }

    private void startObservingUserProfile() {
        String uid = AuthManager.getInstance(this).getUid();
        if (uid == null) return;

        userProfileListener = UserRepository.getInstance().observeUser(uid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (isFinishing() || isDestroyed()) return;
                ProfileUIUtils.loadUserProfile(VoiceChannelActivity.this, user,
                        ivCenterAvatar, null, null, null);
                if (user.getDisplayName() != null) {
                    localDisplayName = user.getDisplayName();
                    tvUserNameStatus.setText(user.getDisplayName());
                    publishLocalVoiceParticipantState();
                }
            }
            @Override
            public void onUserNotFound() {}
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Profile load error: " + error);
            }
        });
    }

    private void setupClickListeners() {
        btnCollapse.setOnClickListener(v -> {
            animateClick(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(1, 1)).build());
                } catch (Exception e) {
                    finish();
                }
            } else {
                finish();
            }
        });

        btnMuteMain.setOnClickListener(v -> {
            animateClick(v);
            isMuted = !isMuted;
            if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(isMuted);
            VoiceStateManager.getInstance().setMuted(isMuted);
            updateMicUiState();
            publishLocalVoiceParticipantState();
            refreshMuteBadgesOnGrid();
        });

        if (btnSpeakerTop != null) {
            btnSpeakerTop.setOnClickListener(v -> {
                animateClick(v);
                isSpeakerOn = !isSpeakerOn;
                if (mRtcEngine != null) mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
                btnSpeakerTop.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
            });
        }

        btnVideo.setOnClickListener(v -> {
            animateClick(v);
            if (!isVideoOn && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQ_ID);
                return;
            }
            isVideoOn = !isVideoOn;
            if (mRtcEngine != null) {
                if (isVideoOn) {
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
                    FrameLayout flLocalPreview = findViewById(R.id.fl_local_preview);
                    if (flLocalPreview != null) flLocalPreview.removeAllViews();
                    androidx.cardview.widget.CardView cvLocalPreview = findViewById(R.id.cv_local_preview);
                    if (cvLocalPreview != null) cvLocalPreview.setVisibility(View.GONE);
                }
                renderVideoGrid();
            }
            VoiceStateManager.getInstance().setVideoOn(isVideoOn);
            updateVideoUiState();
            publishLocalVoiceParticipantState();
        });

        if (btnSwitchCamera != null) {
            btnSwitchCamera.setOnClickListener(v -> {
                animateClick(v);
                if (mRtcEngine != null && isVideoOn) mRtcEngine.switchCamera();
            });
        }

        btnLeaveMain.setOnClickListener(v -> {
            animateClick(v);
            leaveChannel();
        });

        btnChatMain.setOnClickListener(v -> {
            animateClick(v);
            new ChatBottomSheet(channelName).show(getSupportFragmentManager(), "ChatSheet");
        });
        btnEventMain.setOnClickListener(v -> {
            animateClick(v);
            new SoundboardBottomSheet(channelName).show(getSupportFragmentManager(), "SoundboardSheet");
        });
        btnAddUserTop.setOnClickListener(v -> {
            animateClick(v);
            InviteFriendsBottomSheet.newInstanceForChannel(channelName).show(getSupportFragmentManager(), "InviteSheet");
        });
        
        // Stop sharing button in main view
        if (btnStopScreenShareMain != null) {
            btnStopScreenShareMain.setOnClickListener(v -> {
                animateClick(v);
                stopScreenShare();
            });
        }

        // Screen share button in sheet
        View btnScreenShareSheet = findViewById(R.id.btn_screen_share_sheet);
        if (btnScreenShareSheet != null) {
            btnScreenShareSheet.setOnClickListener(v -> {
                animateClick(v);
                if (isScreenSharing || isScreenShareStarting) {
                    stopScreenShare();
                } else {
                    startScreenShare();
                }
            });
        }

        // Logic for switches in sheet
        androidx.appcompat.widget.SwitchCompat switchMuteAll = findViewById(R.id.switch_mute_all);
        if (switchMuteAll != null) {
            switchMuteAll.setOnClickListener(v -> btnMuteMain.performClick());
        }

        androidx.appcompat.widget.SwitchCompat switchVideoOnly = findViewById(R.id.switch_video_only);
        if (switchVideoOnly != null) {
            switchVideoOnly.setOnCheckedChangeListener((bv, checked) -> {
                // Handle video only logic if needed
            });
        }
    }

    private static String safeVoiceChannelKey(String name) {
        if (name == null || name.isEmpty()) return "default";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.length() > 0 ? sb.toString() : "default";
    }

    private void startVoiceAgoraUidMapListener() {
        String safeKey = safeVoiceChannelKey(channelName);
        if (voiceAgoraMapRef != null && voiceAgoraMapListener != null) {
            voiceAgoraMapRef.removeEventListener(voiceAgoraMapListener);
        }
        voiceAgoraMapRef = FirebaseManager.getDatabaseReference("voice_agora_uid_map").child(safeKey);
        voiceAgoraMapListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, String> next = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        int agoraUid = Integer.parseInt(child.getKey());
                        String fbUid = child.getValue(String.class);
                        if (fbUid != null && !fbUid.isEmpty()) {
                            next.put(agoraUid, fbUid);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                runOnUiThread(() -> {
                    agoraUidToFirebaseUid.clear();
                    agoraUidToFirebaseUid.putAll(next);
                    fetchDisplayLabelsForMappedUids();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "voice_agora_uid_map: " + error.getMessage());
            }
        };
        voiceAgoraMapRef.addValueEventListener(voiceAgoraMapListener);
    }

    private void publishLocalAgoraUidMapping() {
        String fbUid = AuthManager.getInstance(this).getUid();
        if (fbUid == null || fbUid.isEmpty() || voiceAgoraMapRef == null || localUid == 0) {
            return;
        }
        voiceAgoraMapRef.child(String.valueOf(localUid)).setValue(fbUid);
    }

    private void removeLocalAgoraUidMapping() {
        String fbUid = AuthManager.getInstance(this).getUid();
        if (fbUid == null || localUid == 0) {
            return;
        }
        String safeKey = safeVoiceChannelKey(channelName);
        FirebaseManager.getDatabaseReference("voice_agora_uid_map")
                .child(safeKey)
                .child(String.valueOf(localUid))
                .removeValue();
    }

    private void startVoiceParticipantStateListener() {
        String safeKey = safeVoiceChannelKey(channelName);
        if (voiceParticipantStateRef != null && voiceParticipantStateListener != null) {
            voiceParticipantStateRef.removeEventListener(voiceParticipantStateListener);
        }
        voiceParticipantStateRef = FirebaseManager.getDatabaseReference("voice_channel_participants").child(safeKey);
        voiceParticipantStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, String> nextLabels = new HashMap<>();
                Set<Integer> nextMuted = new HashSet<>();
                Set<Integer> nextScreenShares = new HashSet<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Integer agoraUidValue = readInt(child.child("agoraUid"));
                    if (agoraUidValue == null || agoraUidValue <= 0) {
                        continue;
                    }

                    int agoraUid = agoraUidValue;
                    String displayName = child.child("displayName").getValue(String.class);
                    Boolean muted = child.child("muted").getValue(Boolean.class);
                    Boolean screenSharing = child.child("screenSharing").getValue(Boolean.class);

                    if (displayName != null && !displayName.trim().isEmpty()) {
                        nextLabels.put(agoraUid, displayName);
                    }
                    if (agoraUid != localUid && Boolean.TRUE.equals(muted)) {
                        nextMuted.add(agoraUid);
                    }
                    if (agoraUid != localUid && Boolean.TRUE.equals(screenSharing)) {
                        nextScreenShares.add(agoraUid);
                    }
                }

                runOnUiThread(() -> {
                    agoraUidToDisplayLabel.putAll(nextLabels);
                    remoteAudioMutedUids.clear();
                    remoteAudioMutedUids.addAll(nextMuted);
                    remoteScreenShareUids.clear();
                    remoteScreenShareUids.addAll(nextScreenShares);
                    updateParticipantCountUi();
                    renderVideoGrid();
                    refreshMuteBadgesOnGrid();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "voice_channel_participants: " + error.getMessage());
            }
        };
        voiceParticipantStateRef.addValueEventListener(voiceParticipantStateListener);
    }

    private void publishLocalVoiceParticipantState() {
        String fbUid = AuthManager.getInstance(this).getUid();
        if (fbUid == null || fbUid.isEmpty() || voiceParticipantStateRef == null || localUid == 0) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("agoraUid", localUid);
        payload.put("displayName", localDisplayName != null && !localDisplayName.trim().isEmpty() ? localDisplayName : tvUserNameStatus.getText().toString());
        payload.put("muted", isMuted);
        payload.put("videoOn", isVideoOn);
        payload.put("screenSharing", isScreenSharing);
        payload.put("updatedAt", System.currentTimeMillis());

        voiceParticipantStateRef.child(fbUid).updateChildren(payload);
        voiceParticipantStateRef.child(fbUid).onDisconnect().removeValue();
    }

    private void removeLocalVoiceParticipantState() {
        String fbUid = AuthManager.getInstance(this).getUid();
        if (fbUid == null || voiceParticipantStateRef == null) {
            return;
        }
        voiceParticipantStateRef.child(fbUid).removeValue();
    }

    @Nullable
    private Integer readInt(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void fetchDisplayLabelsForMappedUids() {
        for (Map.Entry<Integer, String> e : new ArrayList<>(agoraUidToFirebaseUid.entrySet())) {
            final int agoraUid = e.getKey();
            String fbUid = e.getValue();
            if (fbUid == null) {
                continue;
            }
            UserRepository.getInstance().getUserByUid(fbUid, new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    String label = user.getDisplayName();
                    if (label == null || label.isEmpty()) {
                        label = user.getUserName();
                    }
                    if (label == null || label.isEmpty()) {
                        label = fallbackRemoteLabel(agoraUid);
                    }
                    String finalLabel = label;
                    runOnUiThread(() -> {
                        agoraUidToDisplayLabel.put(agoraUid, finalLabel);
                        updateTileLabelsOnly();
                    });
                }

                @Override
                public void onUserNotFound() {
                    runOnUiThread(() -> {
                        agoraUidToDisplayLabel.put(agoraUid, fallbackRemoteLabel(agoraUid));
                        updateTileLabelsOnly();
                    });
                }

                @Override
                public void onFailure(String error) {
                    onUserNotFound();
                }
            });
        }
    }

    private void updateTileLabelsOnly() {
        if (glVideoGrid == null) {
            return;
        }
        for (int i = 0; i < glVideoGrid.getChildCount(); i++) {
            View tile = glVideoGrid.getChildAt(i);
            Object tag = tile.getTag(R.id.tag_voice_tile_uid);
            if (!(tag instanceof Integer)) {
                continue;
            }
            int uid = (Integer) tag;
            if (uid == 0) {
                continue;
            }
            TextView name = tile.findViewById(R.id.tv_user_name);
            if (name == null) {
                continue;
            }
            String label = agoraUidToDisplayLabel.get(uid);
            name.setText(label != null ? label : fallbackRemoteLabel(uid));
        }
    }

    private void refreshMuteBadgesOnGrid() {
        if (glVideoGrid == null) {
            return;
        }
        for (int i = 0; i < glVideoGrid.getChildCount(); i++) {
            View tile = glVideoGrid.getChildAt(i);
            Object tag = tile.getTag(R.id.tag_voice_tile_uid);
            if (!(tag instanceof Integer)) {
                continue;
            }
            int uid = (Integer) tag;
            ImageView mic = tile.findViewById(R.id.iv_tile_mic_muted);
            if (mic == null) {
                continue;
            }
            if (uid == 0) {
                mic.setVisibility(isMuted ? View.VISIBLE : View.GONE);
            } else {
                mic.setVisibility(remoteAudioMutedUids.contains(uid) ? View.VISIBLE : View.GONE);
            }
        }
    }

    private String fallbackRemoteLabel(int agoraUid) {
        return "UID " + agoraUid;
    }

    private void applyCameraPublishToChannel(boolean publishing) {
        if (mRtcEngine == null || !hasJoinedChannel) {
            return;
        }
        ChannelMediaOptions opts = new ChannelMediaOptions();
        opts.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        opts.publishMicrophoneTrack = true;
        if ((isScreenSharing || isScreenShareStarting)
                && screenShareController != null
                && screenShareController.getCustomVideoTrackId() > 0) {
            opts.publishCameraTrack = false;
            opts.publishCustomVideoTrack = true;
            opts.customVideoTrackId = screenShareController.getCustomVideoTrackId();
        } else {
            opts.publishCameraTrack = publishing;
            opts.publishCustomVideoTrack = false;
        }
        opts.autoSubscribeAudio = true;
        opts.autoSubscribeVideo = true;
        int code = mRtcEngine.updateChannelMediaOptions(opts);
        if (code != 0) {
            Log.w(TAG, "updateChannelMediaOptions publishCameraTrack=" + publishing + " code=" + code);
        }
    }

    private void setupLocalVideo() {
        FrameLayout flLocalPreview = findViewById(R.id.fl_local_preview);
        androidx.cardview.widget.CardView cvLocalPreview = findViewById(R.id.cv_local_preview);
        if (flLocalPreview == null) return;

        TextureView textureView = RtcEngine.CreateTextureView(getBaseContext());
        flLocalPreview.removeAllViews();
        flLocalPreview.addView(textureView, new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        if (cvLocalPreview != null) {
            cvLocalPreview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        View controlSheet = findViewById(R.id.control_sheet);
        View bannerAddPeople = findViewById(R.id.banner_add_people);
        View pillChannelName = findViewById(R.id.pill_channel_name);

        int visibility = isInPictureInPictureMode ? View.GONE : View.VISIBLE;

        btnCollapse.setVisibility(visibility);
        if (pillChannelName != null) pillChannelName.setVisibility(visibility);
        btnAddUserTop.setVisibility(visibility);
        if (btnSpeakerTop != null) btnSpeakerTop.setVisibility(visibility);
        if(bannerAddPeople != null) bannerAddPeople.setVisibility(visibility);
        if(controlSheet != null) controlSheet.setVisibility(visibility);
    }

    private boolean hasRequiredCallPermissions() {
        for (String permission : REQUIRED_CALL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestRequiredCallPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_CALL_PERMISSIONS, PERMISSION_REQ_ID);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQ_ID
        );
    }

    private void ensureVoiceChannelReady(boolean shouldStartScreenShareAfterJoin) {
        if (shouldStartScreenShareAfterJoin) {
            pendingScreenShareAfterJoin = true;
        }
        if (mRtcEngine != null || hasJoinedChannel) {
            return;
        }
        if (!hasRequiredCallPermissions()) {
            requestRequiredCallPermissions();
            return;
        }
        initializeAndJoinChannel();
    }

    private void initializeAndJoinChannel() {
        if (hasJoinedChannel || mRtcEngine != null) return;
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);

            mRtcEngine.enableVideo();
            if (isVideoOn) {
                mRtcEngine.enableLocalVideo(true);
                setupLocalVideo();
                mRtcEngine.startPreview();
            }

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.autoSubscribeAudio = true;
            options.autoSubscribeVideo = true;
            options.publishMicrophoneTrack = true;
            options.publishCameraTrack = isVideoOn;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            mRtcEngine.joinChannel(null, channelName, 0, options);
            mRtcEngine.muteLocalAudioStream(isMuted);
            mRtcEngine.enableAudioVolumeIndication(200, 3, true);
        } catch (Exception initError) {
            isScreenShareStarting = false;
            Log.e(TAG, "Agora init error: " + initError.getMessage());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            localUid = uid;
            runOnUiThread(() -> {
                hasJoinedChannel = true;
                publishLocalAgoraUidMapping();
                publishLocalVoiceParticipantState();
                updateParticipantCountUi();
                if (pendingScreenShareAfterJoin) {
                    pendingScreenShareAfterJoin = false;
                    startScreenShare();
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            remoteUids.add(uid);
            runOnUiThread(() -> {
                updateParticipantCountUi();
                renderVideoGrid();
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            remoteUids.remove(uid);
            remoteVideoUids.remove(uid);
            remoteScreenShareUids.remove(uid);
            remoteAudioMutedUids.remove(uid);
            agoraUidToDisplayLabel.remove(uid);
            runOnUiThread(() -> {
                updateParticipantCountUi();
                renderVideoGrid();
            });
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            if (uid == 0) return;
            runOnUiThread(() -> {
                if (muted) {
                    remoteAudioMutedUids.add(uid);
                } else {
                    remoteAudioMutedUids.remove(uid);
                }
                refreshMuteBadgesOnGrid();
            });
        }

        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
            if (muted) remoteVideoUids.remove(uid);
            else remoteVideoUids.add(uid);
            runOnUiThread(() -> renderVideoGrid());
        }

        @Override
        public void onUserEnableVideo(int uid, boolean enabled) {
            if (enabled) {
                remoteVideoUids.add(uid);
            } else {
                remoteVideoUids.remove(uid);
            }
            runOnUiThread(() -> renderVideoGrid());
        }

        @Override
        public void onVideoSizeChanged(io.agora.rtc2.Constants.VideoSourceType source, int uid, int width, int height, int rotation) {
            if (uid == 0) {
                return;
            }
            if (source == io.agora.rtc2.Constants.VideoSourceType.VIDEO_SOURCE_CUSTOM) {
                if (width > 0 && height > 0) {
                    remoteScreenShareUids.add(uid);
                } else {
                    remoteScreenShareUids.remove(uid);
                }
                runOnUiThread(() -> renderVideoGrid());
            }
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            if (state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_STOPPED
                    || state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_FAILED) {
                remoteVideoUids.remove(uid);
                runOnUiThread(() -> renderVideoGrid());
            } else if (state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_DECODING
                    || state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_STARTING) {
                remoteVideoUids.add(uid);
                runOnUiThread(() -> renderVideoGrid());
            }
        }

        @Override
        public void onLocalVideoStateChanged(io.agora.rtc2.Constants.VideoSourceType source, int state, int error) {
            if (source == io.agora.rtc2.Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY) {
                if (state == io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_CAPTURING || 
                    state == io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_ENCODING) {
                    // Screen capture successfully started/encoding!
                    runOnUiThread(() -> {
                        isScreenShareStarting = false;
                        io.agora.rtc2.ChannelMediaOptions options = new io.agora.rtc2.ChannelMediaOptions();
                        options.publishCameraTrack = false;
                        options.publishMicrophoneTrack = true;
                        options.publishScreenCaptureVideo = true;
                        options.publishScreenCaptureAudio = true;
                        if (mRtcEngine != null) {
                            mRtcEngine.updateChannelMediaOptions(options);
                        }
                        isScreenSharing = true;
                        renderVideoGrid();
                        Toast.makeText(VoiceChannelActivity.this, "Đã bắt đầu trình chiếu", Toast.LENGTH_SHORT).show();
                    });
                } else if (state == io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_FAILED) {
                    // User denied or it failed for some reason
                    runOnUiThread(() -> {
                        isScreenShareStarting = false;
                        isScreenSharing = false;
                        renderVideoGrid();
                        if (error == io.agora.rtc2.Constants.ERR_SCREEN_CAPTURE_PERMISSION_DENIED) {
                            Toast.makeText(VoiceChannelActivity.this, "Đã hủy chia sẻ màn hình", Toast.LENGTH_SHORT).show();
                        } else {
                            // Don't spam errors unless relevant, but try to stop it gracefully
                            if (mRtcEngine != null) mRtcEngine.stopScreenCapture();
                        }
                    });
                } else if (state == io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_STOPPED) {
                    // Stopped successfully
                    runOnUiThread(() -> {
                        isScreenShareStarting = false;
                        isScreenSharing = false;
                        renderVideoGrid();
                    });
                }
            }
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            runOnUiThread(() -> {
                speakingUids.clear();
                boolean localTalking = false;
                if (speakers != null) {
                    for (AudioVolumeInfo info : speakers) {
                        if (info.volume <= 5) continue;
                        if (info.uid != 0) {
                            speakingUids.add(info.uid);
                        }
                        if (info.uid == 0 || info.uid == localUid) {
                            localTalking = true;
                        }
                    }
                }

                isLocalSpeaking = localTalking && !isMuted;
                VoiceStateManager.getInstance().setSpeaking(isLocalSpeaking);

                if (vMainSpeakingBorder != null) {
                    vMainSpeakingBorder.setVisibility(isLocalSpeaking ? View.VISIBLE : View.GONE);
                }

                for (Map.Entry<String, View> entry : tileSpeakingBorders.entrySet()) {
                    String key = entry.getKey();
                    View border = entry.getValue();
                    if (border == null) continue;
                    boolean show;
                    if ("local".equals(key)) {
                        show = isLocalSpeaking;
                    } else {
                        try {
                            int uid = Integer.parseInt(key);
                            show = speakingUids.contains(uid);
                        } catch (NumberFormatException e) {
                            show = false;
                        }
                    }
                    border.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    };

    private void renderVideoGrid() {
        if (glVideoGrid == null || mRtcEngine == null) return;

        renderScreenSharePreview();

        List<Integer> activeVideoUids = new ArrayList<>(remoteVideoUids);
        if (isVideoOn) activeVideoUids.add(0); // 0 is local

        if (activeVideoUids.isEmpty()) {
            glVideoGrid.setVisibility(View.GONE);
            if (flCenterAvatarContainer != null) {
                flCenterAvatarContainer.setVisibility(
                        cvScreenShareStatus != null && cvScreenShareStatus.getVisibility() == View.VISIBLE
                                ? View.GONE
                                : View.VISIBLE
                );
            }
            return;
        }

        glVideoGrid.setVisibility(View.VISIBLE);
        if (flCenterAvatarContainer != null) flCenterAvatarContainer.setVisibility(View.GONE);
        
        glVideoGrid.removeAllViews();
        tileSpeakingBorders.clear();

        int total = activeVideoUids.size();
        glVideoGrid.setColumnCount(total > 1 ? 2 : 1);
        glVideoGrid.setRowCount((int) Math.ceil(total / 2.0));

        for (int uid : activeVideoUids) {
            View tile = LayoutInflater.from(this).inflate(R.layout.item_voice_video_tile, glVideoGrid, false);
            FrameLayout slot = tile.findViewById(R.id.fl_video_slot);
            View border = tile.findViewById(R.id.v_speaking_border);
            TextView name = tile.findViewById(R.id.tv_user_name);
            
            String borderKey = uid == 0 ? "local" : String.valueOf(uid);
            tileSpeakingBorders.put(borderKey, border);
            tile.setTag(R.id.tag_voice_tile_uid, uid);
            if (uid == 0) {
                name.setText("Bạn");
            } else {
                String label = agoraUidToDisplayLabel.get(uid);
                name.setText(label != null ? label : fallbackRemoteLabel(uid));
            }

            TextureView tv = RtcEngine.CreateTextureView(getBaseContext());
            slot.addView(tv);

            if (uid == 0) {
                mRtcEngine.setupLocalVideo(new VideoCanvas(tv, VideoCanvas.RENDER_MODE_HIDDEN, 0));
            } else {
                mRtcEngine.setupRemoteVideo(new VideoCanvas(tv, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            tile.setLayoutParams(params);
            
            glVideoGrid.addView(tile);
        }
        refreshMuteBadgesOnGrid();
    }

    private void renderScreenSharePreview() {
        if (cvScreenShareStatus == null || flScreenSharePreview == null || mRtcEngine == null) {
            return;
        }

        Integer shareUid = null;
        boolean localShare = false;
        if (isScreenSharing) {
            shareUid = 0;
            localShare = true;
        } else if (!remoteScreenShareUids.isEmpty()) {
            shareUid = remoteScreenShareUids.iterator().next();
        }

        if (shareUid == null) {
            cvScreenShareStatus.setVisibility(View.GONE);
            flScreenSharePreview.removeAllViews();
            renderedScreenShareUid = null;
            renderedScreenShareIsLocal = false;
            if (ivScreenShareIllustration != null) {
                ivScreenShareIllustration.setVisibility(View.VISIBLE);
            }
            return;
        }

        cvScreenShareStatus.setVisibility(View.VISIBLE);
        if (ivScreenShareIllustration != null) {
            ivScreenShareIllustration.setVisibility(View.GONE);
        }
        if (tvShareLabel != null) {
            String label = localShare ? localDisplayName : agoraUidToDisplayLabel.getOrDefault(shareUid, fallbackRemoteLabel(shareUid));
            tvShareLabel.setText(label);
        }

        if (renderedScreenShareUid != null
                && renderedScreenShareUid == shareUid
                && renderedScreenShareIsLocal == localShare
                && flScreenSharePreview.getChildCount() > 0) {
            return;
        }

        TextureView previewView = RtcEngine.CreateTextureView(getBaseContext());
        flScreenSharePreview.removeAllViews();
        flScreenSharePreview.addView(previewView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        renderedScreenShareUid = shareUid;
        renderedScreenShareIsLocal = localShare;

        int sourceType = Constants.VideoSourceType.VIDEO_SOURCE_CUSTOM.getValue();
        if (localShare) {
            mRtcEngine.setupLocalVideo(new VideoCanvas(previewView, VideoCanvas.RENDER_MODE_FIT, 0, sourceType));
            try {
                mRtcEngine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_CUSTOM);
            } catch (Exception previewError) {
                Log.w(TAG, "Unable to start custom source preview", previewError);
            }
        } else {
            mRtcEngine.setupRemoteVideo(new VideoCanvas(previewView, VideoCanvas.RENDER_MODE_FIT, shareUid, sourceType));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (hasRequiredCallPermissions()) {
                ensureVoiceChannelReady(pendingScreenShareAfterJoin);
            } else {
                pendingScreenShareAfterJoin = false;
                Toast.makeText(this, "Can cap quyen micro va camera de vao voice channel", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQ_ID) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS denied. Voice channel can continue without notification permission.");
            }
            return;
        }

        if (requestCode == CAMERA_PERMISSION_REQ_ID) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                btnVideo.performClick();
            } else {
                Toast.makeText(this, "Can cap quyen camera de bat video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateParticipantCountUi() {
        int count = remoteUids.size() + 1;
        tvParticipantCount.setText(count + " người trong phòng");
    }

    private void leaveChannel() {
        VoiceStateManager.getInstance().leaveChannel();
        pendingScreenShareAfterJoin = false;
        renderedScreenShareUid = null;
        renderedScreenShareIsLocal = false;
        removeLocalVoiceParticipantState();
        if (mRtcEngine != null) {
            if (isScreenSharing || isScreenShareStarting) {
                stopScreenShare();
            }
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        hasJoinedChannel = false;
        if (!isFinishing()) {
            finish();
        }
    }

    public void triggerScreenShareToggle() {
        if (isScreenSharing || isScreenShareStarting) {
            stopScreenShare();
        } else {
            startScreenShare();
        }
    }

    private void startScreenShare() {
        if (mRtcEngine == null) {
            ensureVoiceChannelReady(true);
            Toast.makeText(this, "Dang khoi dong voice channel...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hasJoinedChannel) {
            pendingScreenShareAfterJoin = true;
            Toast.makeText(this, "Dang vao Voice channel, se tu dong bat chia se man hinh sau khi ket noi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFinishing() || isDestroyed() || isScreenSharing || isScreenShareStarting) {
            return;
        }
        if (mediaProjectionManager == null) {
            Toast.makeText(this, "Thiet bi khong ho tro chia se man hinh", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            isScreenShareStarting = true;
            screenSharePermissionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
            Toast.makeText(this, "Vui lòng cấp quyền quay màn hình khi được hỏi...", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            isScreenShareStarting = false;
            Log.e(TAG, "Lỗi khi bắt đầu chia sẻ màn hình: " + Log.getStackTraceString(e));
            Toast.makeText(this, "Thiết bị không hỗ trợ chia sẻ màn hình", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScreenShare() {
        pendingScreenShareAfterJoin = false;
        if (!isScreenSharing && !isScreenShareStarting) return;
        isScreenShareStarting = false;
        clearPendingScreenSharePermission();
        ScreenShareForegroundService.setForegroundReadyListener(null);
        applyScreenSharePublishToChannel(false);
        if (mRtcEngine != null) {
            try {
                mRtcEngine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_CUSTOM);
            } catch (Exception ignored) {
            }
        }
        if (screenShareController != null) {
            screenShareController.stopCapture();
        }
        isScreenSharing = false;
        renderedScreenShareUid = null;
        renderedScreenShareIsLocal = false;
        stopService(ScreenShareForegroundService.createStopIntent(this));
        runOnUiThread(() -> renderVideoGrid());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceStateManager.getInstance().removeListener(this);
        String uid = AuthManager.getInstance(this).getUid();
        if (uid != null && userProfileListener != null) {
            UserRepository.getInstance().removeListener(uid, userProfileListener);
        }
        if (voiceAgoraMapRef != null && voiceAgoraMapListener != null) {
            voiceAgoraMapRef.removeEventListener(voiceAgoraMapListener);
            voiceAgoraMapListener = null;
            voiceAgoraMapRef = null;
        }
        removeLocalAgoraUidMapping();
        removeLocalVoiceParticipantState();
        if (voiceParticipantStateRef != null && voiceParticipantStateListener != null) {
            voiceParticipantStateRef.removeEventListener(voiceParticipantStateListener);
            voiceParticipantStateListener = null;
            voiceParticipantStateRef = null;
        }
        pendingScreenShareAfterJoin = false;
        ScreenShareForegroundService.setForegroundReadyListener(null);
        if (mRtcEngine != null) {
            if (isScreenSharing || isScreenShareStarting) {
                stopScreenShare();
            }
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        hasJoinedChannel = false;
    }

    private void startScreenShareForegroundService() {
        if (pendingScreenShareData == null) {
            isScreenShareStarting = false;
            return;
        }
        ScreenShareForegroundService.setForegroundReadyListener(() ->
                runOnUiThread(this::beginScreenShareCapture)
        );
        ContextCompat.startForegroundService(this, ScreenShareForegroundService.createStartIntent(this));
    }

    private void beginScreenShareCapture() {
        if (mRtcEngine == null || pendingScreenShareData == null || isFinishing() || isDestroyed()) {
            isScreenShareStarting = false;
            clearPendingScreenSharePermission();
            stopService(ScreenShareForegroundService.createStopIntent(this));
            return;
        }

        if (screenShareController == null) {
            screenShareController = new AgoraScreenShareController(getApplicationContext(), new AgoraScreenShareController.Callback() {
                @Override
                public void onScreenShareStarted(int customVideoTrackId) {
                    runOnUiThread(() -> {
                        isScreenShareStarting = false;
                        isScreenSharing = true;
                        applyScreenSharePublishToChannel(true);
                        publishLocalVoiceParticipantState();
                        renderVideoGrid();
                        Toast.makeText(VoiceChannelActivity.this, "Da bat dau chia se man hinh", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onScreenShareStopped() {
                    runOnUiThread(() -> handleScreenShareStoppedUi(false));
                }

                @Override
                public void onScreenShareError(String userMessage, @Nullable Throwable error) {
                    Log.e(TAG, userMessage, error);
                    runOnUiThread(() -> {
                        handleScreenShareStoppedUi(true);
                        Toast.makeText(VoiceChannelActivity.this, userMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

        Intent resultData = pendingScreenShareData;
        int resultCode = pendingScreenShareResultCode;
        clearPendingScreenSharePermission();

        boolean started = screenShareController.startCapture(mRtcEngine, resultCode, resultData);
        if (!started) {
            handleScreenShareStoppedUi(true);
        }
    }

    private void applyScreenSharePublishToChannel(boolean publishing) {
        if (mRtcEngine == null || !hasJoinedChannel) {
            return;
        }
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.publishMicrophoneTrack = true;
        options.publishCameraTrack = !publishing && isVideoOn;
        options.publishCustomVideoTrack = publishing;
        options.autoSubscribeAudio = true;
        options.autoSubscribeVideo = true;
        if (publishing && screenShareController != null && screenShareController.getCustomVideoTrackId() > 0) {
            options.customVideoTrackId = screenShareController.getCustomVideoTrackId();
        }
        int code = mRtcEngine.updateChannelMediaOptions(options);
        if (code != 0) {
            Log.w(TAG, "updateChannelMediaOptions screenShare=" + publishing + " code=" + code);
        }
    }

    private void clearPendingScreenSharePermission() {
        pendingScreenShareResultCode = RESULT_CANCELED;
        pendingScreenShareData = null;
    }

    private void handleScreenShareStoppedUi(boolean stopForegroundService) {
        isScreenShareStarting = false;
        isScreenSharing = false;
        pendingScreenShareAfterJoin = false;
        renderedScreenShareUid = null;
        renderedScreenShareIsLocal = false;
        clearPendingScreenSharePermission();
        ScreenShareForegroundService.setForegroundReadyListener(null);
        applyScreenSharePublishToChannel(false);
        publishLocalVoiceParticipantState();
        if (stopForegroundService || !isFinishing()) {
            stopService(ScreenShareForegroundService.createStopIntent(this));
        }
        renderVideoGrid();
    }
}
