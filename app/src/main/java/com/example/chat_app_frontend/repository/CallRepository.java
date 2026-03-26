package com.example.chat_app_frontend.repository;

import com.example.chat_app_frontend.model.CallSession;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CallRepository {
    private static final String PATH_CALL_SESSIONS = "call_sessions";

    private static CallRepository instance;
    private final DatabaseReference callSessionsRef;

    private CallRepository() {
        callSessionsRef = FirebaseManager.getDatabaseReference(PATH_CALL_SESSIONS);
    }

    public static synchronized CallRepository getInstance() {
        if (instance == null) {
            instance = new CallRepository();
        }
        return instance;
    }

    public static String buildCallId(String uidA, String uidB) {
        if (uidA == null) uidA = "";
        if (uidB == null) uidB = "";
        return uidA.compareTo(uidB) <= 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public static String buildChannelName(String callId) {
        String base = "c_" + callId;
        return base.length() > 64 ? base.substring(0, 64) : base;
    }

    public void createRingingSession(String callId, String callerUid, String calleeUid, boolean isVideo) {
        long now = System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("callId", callId);
        payload.put("callerUid", callerUid);
        payload.put("calleeUid", calleeUid);
        payload.put("callType", isVideo ? "video" : "audio");
        payload.put("channelName", buildChannelName(callId));
        payload.put("status", "ringing");
        payload.put("createdAt", now);
        payload.put("updatedAt", now);
        payload.put("inviteSentAt", null);
        callSessionsRef.child(callId).updateChildren(payload);
    }

    public void updateStatus(String callId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("updatedAt", System.currentTimeMillis());
        if ("accepted".equals(status)) {
            payload.put("acceptedAt", System.currentTimeMillis());
        }
        if ("ended".equals(status) || "rejected".equals(status)) {
            payload.put("endedAt", System.currentTimeMillis());
        }
        callSessionsRef.child(callId).updateChildren(payload);
    }

    public ValueEventListener observeSession(String callId, ValueEventListener listener) {
        callSessionsRef.child(callId).addValueEventListener(listener);
        return listener;
    }

    public void removeObserver(String callId, ValueEventListener listener) {
        if (listener == null) return;
        callSessionsRef.child(callId).removeEventListener(listener);
    }

    public DatabaseReference getSessionRef(String callId) {
        return callSessionsRef.child(callId);
    }
}
