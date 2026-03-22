package com.example.chat_app_frontend.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.NamePlate;
import com.example.chat_app_frontend.model.ProfileEffect;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.DecorationRepository;
import com.example.chat_app_frontend.repository.NamePlateRepository;
import com.example.chat_app_frontend.repository.ProfileEffectRepository;

/**
 * Utility class to consistently load and render user profile customizations
 * (Decorations, Name Plates, Profile Effects) across different UI components.
 */
public class ProfileUIUtils {

    /**
     * Loads the full profile UI for a user into the provided views.
     */
    public static void loadUserProfile(Context context, User user, 
                                      @Nullable ImageView imgAvatar,
                                      @Nullable ImageView imgDecoration,
                                      @Nullable ImageView imgNamePlate,
                                      @Nullable ImageView imgProfileEffect) {
        if (user == null || context == null) return;

        // 1. Avatar
        if (imgAvatar != null) {
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                imgAvatar.setVisibility(View.VISIBLE);
                Glide.with(context).load(avatarUrl).into(imgAvatar);
            } else {
                // Default avatar
                imgAvatar.setVisibility(View.VISIBLE);
                imgAvatar.setImageResource(com.example.chat_app_frontend.R.drawable.img_discord);
            }
        }

        // 2. Decoration
        if (imgDecoration != null) {
            String decId = user.getAvatarDecorationId();
            Decoration dec = DecorationRepository.getInstance().findDecorationById(decId);
            if (dec != null && dec.getType() != Decoration.Type.NONE) {
                imgDecoration.setVisibility(View.VISIBLE);
                Glide.with(context).load(dec.getDrawableResId()).into(imgDecoration);
            } else {
                imgDecoration.setVisibility(View.GONE);
            }
        }

        // 3. Name Plate (Background)
        if (imgNamePlate != null) {
            String plateId = user.getNamePlateId();
            NamePlate plate = NamePlateRepository.getInstance().findNamePlateById(plateId);
            if (plate != null && plate.getType() != NamePlate.Type.NONE && plate.getType() != NamePlate.Type.STORE) {
                imgNamePlate.setVisibility(View.VISIBLE);
                Glide.with(context).load(plate.getDrawableResId()).into(imgNamePlate);
            } else {
                imgNamePlate.setVisibility(View.GONE);
            }
        }

        // 4. Profile Effect (Animated overlay)
        if (imgProfileEffect != null) {
            String effectId = user.getProfileEffectId();
            ProfileEffect effect = ProfileEffectRepository.getInstance().findEffectById(effectId);
            if (effect != null && effect.getType() != ProfileEffect.Type.NONE && effect.getType() != ProfileEffect.Type.SHOP) {
                imgProfileEffect.setVisibility(View.VISIBLE);
                Glide.with(context).load(effect.getEffectResId()).into(imgProfileEffect);
            } else {
                imgProfileEffect.setVisibility(View.GONE);
            }
        }
    }
}
