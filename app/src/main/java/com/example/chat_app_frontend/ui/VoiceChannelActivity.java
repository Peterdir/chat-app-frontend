package com.example.chat_app_frontend.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Rational;
import android.view.View;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.view.TextureView;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.example.chat_app_frontend.utils.FirebaseManager;
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

public class VoiceChannelActivity extends AppCompatActivity {
    private static final String TAG = "VoiceChannelActivity";
    private static final int PERMISSION_REQ_ID = 22;

    // View declarations
    private TextView tvTopChannelName, tvParticipantCount, tvUserNameStatus;
    private ImageButton btnCollapse, btnAddUserTop, btnSpeakerTop;
    private ImageButton btnVideo, btnSwitchCamera, btnMuteMain, btnChatMain, btnEventMain, btnLeaveMain;

    private FrameLayout flCenterAvatarContainer;
    private GridLayout glVideoGrid;
    private View vMainSpeakingBorder;
    private ImageView ivCenterAvatar;

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

    // User tracking
    private int localUid = 0; 
    private final Set<Integer> remoteUids = new HashSet<>();
    private final Set<Integer> remoteVideoUids = new HashSet<>();
    private final Set<Integer> speakingUids = new HashSet<>();
    private final Set<Integer> remoteAudioMutedUids = new HashSet<>();
    private final Map<Integer, String> agoraUidToFirebaseUid = new HashMap<>();
    private final Map<Integer, String> agoraUidToDisplayLabel = new HashMap<>();

    private DatabaseReference voiceAgoraMapRef;
    private ValueEventListener voiceAgoraMapListener;

    // UI mapping for speaking borders in grid
    private final Map<String, View> tileSpeakingBorders = new HashMap<>();

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
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

        if (checkPermissions()) {
            initializeAndJoinChannel();
        }
    }

    private void initViews() {
        tvTopChannelName = findViewById(R.id.tv_top_channel_name);
        tvParticipantCount = findViewById(R.id.tv_participant_count);
        tvUserNameStatus = findViewById(R.id.tv_user_name_status);

        btnCollapse = findViewById(R.id.btn_collapse);
        btnAddUserTop = findViewById(R.id.btn_add_user_top);
        btnSpeakerTop = findViewById(R.id.btn_speaker_top);

        btnVideo = findViewById(R.id.btn_video);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnMuteMain = findViewById(R.id.btn_mute_main);
        btnChatMain = findViewById(R.id.btn_chat_main);
        btnEventMain = findViewById(R.id.btn_event_main);
        btnLeaveMain = findViewById(R.id.btn_leave_main);

        flCenterAvatarContainer = findViewById(R.id.fl_center_avatar_container);
        glVideoGrid = findViewById(R.id.gl_video_grid);
        vMainSpeakingBorder = findViewById(R.id.v_main_speaking_border);

        ivCenterAvatar = findViewById(R.id.iv_center_avatar);
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
        } else {
            btnVideo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B2D31")));
            btnVideo.setImageTintList(ColorStateList.valueOf(Color.WHITE));
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
            btnMuteMain.setImageResource(R.drawable.ic_mic_on); // Assuming you have ic_mic_on

            ImageView ivUserMicStatus = findViewById(R.id.iv_user_mic_status);
            if (ivUserMicStatus != null) {
                ivUserMicStatus.setImageResource(R.drawable.ic_mic_on);
                ivUserMicStatus.setColorFilter(Color.parseColor("#43B581"));
            }
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
                    tvUserNameStatus.setText(user.getDisplayName());
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
        });

        btnSwitchCamera.setOnClickListener(v -> {
            animateClick(v);
            if (mRtcEngine != null && isVideoOn) mRtcEngine.switchCamera();
        });

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
        opts.publishCameraTrack = publishing;
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
        View bottomActionBar = findViewById(R.id.bottom_action_bar);
        View bannerAddPeople = findViewById(R.id.banner_add_people);
        View pillChannelName = findViewById(R.id.pill_channel_name);

        int visibility = isInPictureInPictureMode ? View.GONE : View.VISIBLE;

        btnCollapse.setVisibility(visibility);
        if (pillChannelName != null) pillChannelName.setVisibility(visibility);
        btnAddUserTop.setVisibility(visibility);
        if (btnSpeakerTop != null) btnSpeakerTop.setVisibility(visibility);
        if(bannerAddPeople != null) bannerAddPeople.setVisibility(visibility);
        if(bottomActionBar != null) bottomActionBar.setVisibility(visibility);
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

    private void initializeAndJoinChannel() {
        if (hasJoinedChannel) return;
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
            hasJoinedChannel = true;

            mRtcEngine.enableAudioVolumeIndication(200, 3, true);
        } catch (Exception e) {
            Log.e(TAG, "Agora init error: " + e.getMessage());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            localUid = uid;
            runOnUiThread(() -> {
                publishLocalAgoraUidMapping();
                updateParticipantCountUi();
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
        
        List<Integer> activeVideoUids = new ArrayList<>(remoteVideoUids);
        if (isVideoOn) activeVideoUids.add(0); // 0 is local

        if (activeVideoUids.isEmpty()) {
            glVideoGrid.setVisibility(View.GONE);
            if (flCenterAvatarContainer != null) flCenterAvatarContainer.setVisibility(View.VISIBLE);
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

    private void updateParticipantCountUi() {
        int count = remoteUids.size() + 1;
        tvParticipantCount.setText(count + " người trong phòng");
    }

    private void leaveChannel() {
        VoiceStateManager.getInstance().leaveChannel();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
