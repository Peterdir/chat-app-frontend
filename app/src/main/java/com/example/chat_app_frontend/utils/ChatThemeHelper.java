package com.example.chat_app_frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.example.chat_app_frontend.R;

/**
 * Helper class to manage chat background themes.
 * Stores theme selection per conversation in SharedPreferences (local only).
 */
public class ChatThemeHelper {

    private static final String PREF_NAME = "chat_theme_prefs";
    private static final String KEY_PREFIX = "theme_";

    /**
     * Available theme drawable resource IDs.
     * Index 0 = default (discord_background color), 1-5 = gradient themes.
     */
    public static final int[] THEME_DRAWABLES = {
            0, // 0 = default, no drawable – use discord_background color
            R.drawable.bg_chat_theme_1,  // Blurple
            R.drawable.bg_chat_theme_2,  // Sunset
            R.drawable.bg_chat_theme_3,  // Ocean
            R.drawable.bg_chat_theme_4,  // Galaxy
            R.drawable.bg_chat_theme_5   // Midnight
    };

    /**
     * Theme names for display in the picker.
     */
    public static final String[] THEME_NAMES = {
            "Mặc định",
            "Blurple",
            "Sunset",
            "Ocean",
            "Galaxy",
            "Midnight"
    };

    /**
     * Preview colors for each theme (used to render circular swatches).
     * Each is the dominant/center color of the gradient.
     */
    public static final int[] THEME_PREVIEW_COLORS = {
            0xFF313338, // Default – discord_background
            0xFF2A2A6E, // Blurple
            0xFF6B2D5B, // Sunset
            0xFF0E4D64, // Ocean
            0xFF5C2D91, // Galaxy
            0xFF1A1A2E  // Midnight
    };

    /**
     * Save the selected theme index for a given chat ID.
     */
    public static void saveTheme(Context context, String chatId, int themeIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_PREFIX + chatId, themeIndex).apply();
    }

    /**
     * Get the saved theme index for a given chat ID. Returns 0 (default) if not set.
     */
    public static int getTheme(Context context, String chatId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int index = prefs.getInt(KEY_PREFIX + chatId, 0);
        if (index < 0 || index >= THEME_DRAWABLES.length) {
            return 0;
        }
        return index;
    }

    /**
     * Apply the theme background to the given root view.
     *
     * @param rootView   The root ConstraintLayout of the chat activity.
     * @param themeIndex Index into THEME_DRAWABLES (0 = default color).
     */
    public static void applyTheme(View rootView, int themeIndex) {
        if (rootView == null) return;
        if (themeIndex <= 0 || themeIndex >= THEME_DRAWABLES.length) {
            // Default: reset to discord_background color
            rootView.setBackgroundResource(R.color.discord_background);
        } else {
            rootView.setBackgroundResource(THEME_DRAWABLES[themeIndex]);
        }
    }
}
