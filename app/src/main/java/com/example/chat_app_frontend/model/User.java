package com.example.chat_app_frontend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model User cho Android app
 * Tương ứng với User entity từ backend
 *
 * Key trong Firebase RTDB: users/{firebaseUid}
 * Credentials (email/password) được quản lý hoàn toàn bởi Firebase Authentication.
 */
public class User {
    private String firebaseUid;   // Firebase Auth UID – khóa chính trong RTDB
    private String userName;
    private String email;
    private String displayName;
    private String birthDate;     // Format: "yyyy-MM-dd"
    private String country;
    private String pronouns;
    private long createdAt;       // Unix timestamp (milliseconds)
    private long lastActive;      // Unix timestamp (milliseconds)
    private String bio;
    private String avatarUrl;     // URL hoặc Firebase Storage path
    private boolean isActive;
    private boolean isEmailVerified;
    private UserStatus status;
    private List<String> roles;   // ["USER", "ADMIN"]

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Constructor rỗng bắt buộc để Firebase RTDB có thể deserialize */
    public User() {
        this.status = UserStatus.OFFLINE;
        this.roles = new ArrayList<>();
        this.isActive = true;
        this.isEmailVerified = false;
    }

    public User(String firebaseUid, String userName, String email, String displayName) {
        this();
        this.firebaseUid = firebaseUid;
        this.userName = userName;
        this.email = email;
        this.displayName = displayName;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    public boolean isOnline() {
        return status == UserStatus.ONLINE;
    }

    public String getDisplayNameOrUserName() {
        return displayName != null && !displayName.isEmpty() ? displayName : userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + firebaseUid + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", status=" + status +
                '}';
    }
}
