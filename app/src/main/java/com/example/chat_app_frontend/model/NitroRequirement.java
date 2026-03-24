package com.example.chat_app_frontend.model;

/**
 * Điều kiện gói để dùng khung trang trí / hiệu ứng hồ sơ (khớp plan lưu trong RTDB sau VNPay).
 */
public enum NitroRequirement {
    /** Không cần gói (None, Cửa hàng). */
    NONE,
    /** Cần đăng ký Basic hoặc Nitro đang hoạt động. */
    BASIC_OR_FULL,
    /** Chỉ gói Nitro đầy đủ (plan NITRO). */
    FULL_NITRO_ONLY
}
