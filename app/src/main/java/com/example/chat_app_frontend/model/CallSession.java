package com.example.chat_app_frontend.model;

public class CallSession {
    private String callId;
    private String callerUid;
    private String calleeUid;
    private String callType; // "audio" | "video"
    private String channelName;
    private String status; // "ringing" | "accepted" | "rejected" | "ended"
    private Long createdAt;
    private Long updatedAt;
    private Long acceptedAt;
    private Long endedAt;

    public CallSession() {
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getCallerUid() {
        return callerUid;
    }

    public void setCallerUid(String callerUid) {
        this.callerUid = callerUid;
    }

    public String getCalleeUid() {
        return calleeUid;
    }

    public void setCalleeUid(String calleeUid) {
        this.calleeUid = calleeUid;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Long endedAt) {
        this.endedAt = endedAt;
    }
}
