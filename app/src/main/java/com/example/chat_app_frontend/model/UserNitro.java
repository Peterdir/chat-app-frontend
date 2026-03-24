package com.example.chat_app_frontend.model;

/**
 * Snapshot gói Nitro trên Firebase: users/{uid}/nitro
 */
public class UserNitro {

    private String plan;
    private Boolean isActive;
    private Long expiresAt;
    private Long activeSince;
    private Long updatedAt;

    public UserNitro() {
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getActiveSince() {
        return activeSince;
    }

    public void setActiveSince(Long activeSince) {
        this.activeSince = activeSince;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
