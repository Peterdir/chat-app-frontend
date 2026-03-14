package com.example.chat_app_frontend.manager;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;

import com.example.chat_app_frontend.R;

/**
 * Singleton class to manage the global state of the user's voice connection.
 * Tracks which channel is connected, when they joined, and their current
 * activity status.
 */
public class VoiceStateManager {

    private static VoiceStateManager instance;

    private String connectedChannelName = null;
    private long joinTimeMillis = 0;
    private String currentActivityStatus = null; // e.g., "chơi-game", "đang chill"
    private boolean isMuted = true;
    private boolean isVideoOn = false;
    private boolean isSpeaking = false;

    private List<VoiceStateListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface VoiceStateListener {
        void onVoiceStateChanged();
    }

    private VoiceStateManager() {
    }

    public static synchronized VoiceStateManager getInstance() {
        if (instance == null) {
            instance = new VoiceStateManager();
        }
        return instance;
    }

    public void addListener(VoiceStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(VoiceStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        mainHandler.post(() -> {
            for (VoiceStateListener listener : listeners) {
                if (listener != null) {
                    listener.onVoiceStateChanged();
                }
            }
        });
    }

    // --- State setters & getters --- //

    public void setConnectedChannel(String channelName) {
        this.connectedChannelName = channelName;
        this.joinTimeMillis = System.currentTimeMillis();
        notifyListeners();
    }

    public void leaveChannel() {
        this.connectedChannelName = null;
        this.joinTimeMillis = 0;
        this.currentActivityStatus = null;
        this.isVideoOn = false;
        this.isSpeaking = false;
        notifyListeners();
    }

    public String getConnectedChannelName() {
        return connectedChannelName;
    }

    public long getJoinTimeMillis() {
        return joinTimeMillis;
    }

    public void setCurrentActivityStatus(String status) {
        this.currentActivityStatus = status;
        notifyListeners();
    }

    public String getCurrentActivityStatus() {
        return currentActivityStatus;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        notifyListeners();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setVideoOn(boolean videoOn) {
        this.isVideoOn = videoOn;
        notifyListeners();
    }

    public boolean isVideoOn() {
        return isVideoOn;
    }

    public void setSpeaking(boolean speaking) {
        if (this.isSpeaking != speaking) {
            this.isSpeaking = speaking;
            android.util.Log.d("VoiceStateManager", "setSpeaking: " + speaking);
            notifyListeners();
        }
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public int getIconResourceForStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return R.drawable.ic_controller;
        }
        switch (status.trim().toLowerCase()) {
            case "đang chill":
                return R.drawable.ic_status_chill;
            case "đang học":
                return R.drawable.ic_status_study;
            case "sẽ trở lại ngay":
                return R.drawable.ic_status_flight;
            case "đang xem linh tinh":
                return R.drawable.ic_status_mailbox;
            default:
                return R.drawable.ic_controller;
        }
    }
}
