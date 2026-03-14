package com.example.chat_app_frontend.model;

/**
 * Enum đại diện cho trạng thái online/offline của user
 */
public enum UserStatus {
    ONLINE,     // Đang online
    OFFLINE,    // Offline
    AWAY,       // Vắng mặt
    DO_NOT_DISTURB  // Không làm phiền (Busy)
}
