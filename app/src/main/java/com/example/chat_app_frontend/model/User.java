package com.example.chat_app_frontend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model User cho Android app
 * Tương ứng với User entity từ backend
 */
public class User {
    private Long id;
    private String userName;
    private String email;
    private String password;        // Chỉ dùng khi đăng ký/đăng nhập, không lưu local
    private String displayName;
    private String birthDate;       // Format: "yyyy-MM-dd" hoặc timestamp
    private String country;
    private String pronouns;
    private long createdAt;         // Unix timestamp (milliseconds)
    private long lastActive;        // Unix timestamp (milliseconds)
    private String bio;
    private String avatarUrl;       // URL hoặc Firebase Storage path
    private boolean isActive;
    private boolean isEmailVerified;
    private UserStatus status;
    private List<String> roles;     // Đơn giản hóa thành list string: ["USER", "ADMIN"]

    // =========================================================================
    // Constructors
    // =========================================================================

    public User() {
        this.status = UserStatus.OFFLINE;
        this.roles = new ArrayList<>();
        this.isActive = true;
        this.isEmailVerified = false;
    }

    public User(Long id, String userName, String email, String displayName) {
        this();
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.displayName = displayName;
    }

    // Constructor đầy đủ cho Firebase
    public User(Long id, String userName, String email, String displayName,
                String avatarUrl, String bio, UserStatus status) {
        this(id, userName, email, displayName);
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.status = status;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    /**
     * Kiểm tra user có quyền admin không
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    /**
     * Kiểm tra user có đang online không
     */
    public boolean isOnline() {
        return status == UserStatus.ONLINE;
    }

    /**
     * Lấy display name hoặc username nếu không có display name
     */
    public String getDisplayNameOrUserName() {
        return displayName != null && !displayName.isEmpty() ? displayName : userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", status=" + status +
                '}';
    }
}
