package com.example.chat_app_frontend.utils;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Custom Glide module.
 * The APNG4Android glide-plugin 3.0.5 handles its own registration.
 */
@GlideModule
public final class ChatAppGlideModule extends AppGlideModule {
    // Empty implementation as the plugin handles registration automatically
}
