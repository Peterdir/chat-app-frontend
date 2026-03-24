package com.example.chat_app_frontend.utils;

import com.example.chat_app_frontend.model.NitroRequirement;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserNitro;

public final class NitroEligibility {

    private NitroEligibility() {
    }

    public static boolean isSubscriptionActive(User user) {
        if (user == null) return false;
        UserNitro n = user.getNitro();
        if (n == null) return false;
        if (n.getIsActive() == null || !n.getIsActive()) return false;
        if (n.getExpiresAt() == null || n.getExpiresAt() <= 0L) return false;
        return n.getExpiresAt() > System.currentTimeMillis();
    }

    public static boolean hasBasicOrFull(User user) {
        if (!isSubscriptionActive(user)) return false;
        String p = user.getNitro().getPlan();
        return p != null && ("BASIC".equalsIgnoreCase(p) || "NITRO".equalsIgnoreCase(p));
    }

    public static boolean hasFullNitro(User user) {
        if (!isSubscriptionActive(user)) return false;
        String p = user.getNitro().getPlan();
        return p != null && "NITRO".equalsIgnoreCase(p);
    }

    /**
     * BASIC và NITRO đều mở khóa hiệu ứng trả phí; chỉ user không gói bị giới hạn mặc định.
     * FULL_NITRO_ONLY được xử lý giống BASIC_OR_FULL để đồng bộ UX.
     */
    public static boolean canUse(User user, NitroRequirement requirement) {
        if (requirement == null || requirement == NitroRequirement.NONE) {
            return true;
        }
        if (requirement == NitroRequirement.BASIC_OR_FULL
                || requirement == NitroRequirement.FULL_NITRO_ONLY) {
            return hasBasicOrFull(user);
        }
        return false;
    }

    public static String subscriptionLabel(User user) {
        if (!isSubscriptionActive(user)) {
            return null;
        }
        String p = user.getNitro().getPlan();
        if (p == null) return null;
        if ("NITRO".equalsIgnoreCase(p)) return "Nitro";
        if ("BASIC".equalsIgnoreCase(p)) return "Nitro Basic";
        return p;
    }
}
