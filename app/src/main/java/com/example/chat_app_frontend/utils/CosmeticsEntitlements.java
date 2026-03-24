package com.example.chat_app_frontend.utils;

import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.NamePlate;
import com.example.chat_app_frontend.model.ProfileEffect;
import com.example.chat_app_frontend.model.User;

/**
 * Quyền trang trí hiển thị/lưu theo gói Nitro thực tế (không mock “đã sở hữu”).
 */
public final class CosmeticsEntitlements {

    private CosmeticsEntitlements() {
    }

    public static boolean canEquipDecoration(User user, Decoration decoration) {
        if (decoration == null || decoration.getType() == Decoration.Type.NONE
                || decoration.getType() == Decoration.Type.STORE) {
            return true;
        }
        if (!decoration.isNitro()) {
            return true;
        }
        return NitroEligibility.hasBasicOrFull(user);
    }

    public static boolean canEquipNamePlate(User user, NamePlate plate) {
        if (plate == null || plate.getType() == NamePlate.Type.NONE
                || plate.getType() == NamePlate.Type.STORE) {
            return true;
        }
        if (!plate.isLocked() && !plate.isNitro()) {
            return true;
        }
        return NitroEligibility.hasBasicOrFull(user);
    }

    public static boolean canEquipProfileEffect(User user, ProfileEffect effect) {
        if (effect == null || effect.getType() == ProfileEffect.Type.NONE
                || effect.getType() == ProfileEffect.Type.SHOP) {
            return true;
        }
        return NitroEligibility.canUse(user, effect.getNitroRequirement());
    }

    public static Decoration effectiveDecoration(User user, Decoration decoration) {
        if (decoration == null) return null;
        if (canEquipDecoration(user, decoration)) return decoration;
        return null;
    }

    public static ProfileEffect effectiveProfileEffect(User user, ProfileEffect effect) {
        if (effect == null) return null;
        if (canEquipProfileEffect(user, effect)) return effect;
        return null;
    }

    public static NamePlate effectiveNamePlate(User user, NamePlate plate) {
        if (plate == null) return null;
        if (canEquipNamePlate(user, plate)) return plate;
        return null;
    }
}
