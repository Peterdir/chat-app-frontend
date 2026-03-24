package com.example.chat_app_frontend.repository;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.NitroRequirement;
import com.example.chat_app_frontend.model.ProfileEffect;

import java.util.ArrayList;
import java.util.List;

public class ProfileEffectRepository {
    private static ProfileEffectRepository instance;
    private final List<ProfileEffect> allEffects;

    private ProfileEffectRepository() {
        allEffects = new ArrayList<>();
        // Metadata effects
        allEffects.add(new ProfileEffect("none", "Không", "", 0, 0, ProfileEffect.Type.NONE));
        allEffects.add(new ProfileEffect("store", "Cửa hàng", "", 0, 0, ProfileEffect.Type.SHOP));

        NitroRequirement paid = NitroRequirement.BASIC_OR_FULL;

        allEffects.add(new ProfileEffect("magic_mists", "Sương Mù Huyền Bí", "Làn sương huyền bí bao quanh hồ sơ của bạn", R.drawable.magic_mists, R.drawable.magic_mists, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("infinite_swirl", "Vòng Xoáy Vô Tận", "Vòng xoáy ma thuật rực rỡ", R.drawable.infinite_swirl_bundle2, R.drawable.infinite_swirl_bundle2, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("dark_roses", "Hoa Hồng Đen", "Vẻ đẹp bí ẩn của bóng tối", R.drawable.darkroses, R.drawable.darkroses, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("shadow_strikes", "Nhát Chém Bóng Tối", "Hiệu ứng mạnh mẽ từ hư không", R.drawable.shadowstrikes, R.drawable.shadowstrikes, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("nevermore", "Nevermore", "Sự u tối lãng mạn", R.drawable.nevermore, R.drawable.nevermore, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("hologram", "Hologram", "Công nghệ tương lai rực rỡ", R.drawable.hologram, R.drawable.hologram, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("aries", "Aries", "Biểu tượng cung Bạch Dương", R.drawable.aries, R.drawable.aries, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("bonsai", "Bonsai", "Sự tĩnh lặng của thiên nhiên", R.drawable.bonsai, R.drawable.bonsai, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("magic_mists_2", "Sương Mù Ảo Ảnh", "Sắc màu huyền ảo", R.drawable.magic_mists, R.drawable.magic_mists, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("infinite_swirl_2", "Vòng Xoáy Thiên Hà", "Năng lượng vũ trụ", R.drawable.infinite_swirl_bundle2, R.drawable.infinite_swirl_bundle2, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("dark_roses_2", "Hoa Hồng Đêm", "Bí ẩn và quyến rũ", R.drawable.darkroses, R.drawable.darkroses, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("infinite_swirl_1", "Vòng Xoáy Lửa", "Năng lượng rực cháy", R.drawable.infinite_swrirl_bundle, R.drawable.infinite_swrirl_bundle, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("magic_mists_3", "Sương Mù Tím", "Huyền bí và ảo mộng", R.drawable.magic_mists, R.drawable.magic_mists, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("shame_duck", "Vịt Xấu Hổ", "Dễ thương và hài hước", R.drawable.theshameduck, R.drawable.theshameduck, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("song_tu", "Song Tử", "Hiệu ứng đặc quyền Nitro", R.drawable.infinite_swrirl_bundle, R.drawable.infinite_swrirl_bundle, ProfileEffect.Type.EFFECT, paid));
        allEffects.add(new ProfileEffect("wumpus_happy", "Wumpus Vui Vẻ", "Niềm vui từ Wumpus", R.drawable.wumpus_happy, R.drawable.wumpus_happy, ProfileEffect.Type.EFFECT, paid));
    }

    public static synchronized ProfileEffectRepository getInstance() {
        if (instance == null) {
            instance = new ProfileEffectRepository();
        }
        return instance;
    }

    public List<ProfileEffect> getAllEffects() {
        return allEffects;
    }

    /**
     * Danh mục hiệu ứng (quyền dùng do {@link com.example.chat_app_frontend.utils.NitroEligibility} kiểm tra theo user).
     */
    public List<ProfileEffect> getAvailableEffects() {
        return new ArrayList<>(allEffects);
    }

    public ProfileEffect findEffectById(String id) {
        if (id == null) return findEffectById("none");
        for (ProfileEffect effect : allEffects) {
            if (effect.getId().equals(id)) {
                return effect;
            }
        }
        return findEffectById("none");
    }
}
