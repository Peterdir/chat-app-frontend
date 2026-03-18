package com.example.chat_app_frontend.model;

import com.example.chat_app_frontend.R;

public class ProfileEffect {
    public enum Type {
        NONE,
        SHOP,
        EFFECT
    }

    private String id;
    private String name;
    private String description;
    private int thumbnailResId;
    private int effectResId; // Could be a Lottie animation or image
    private Type type;

    public ProfileEffect(String id, String name, String description, int thumbnailResId, int effectResId, Type type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailResId = thumbnailResId;
        this.effectResId = effectResId;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public int getEffectResId() {
        return effectResId;
    }

    public Type getType() {
        return type;
    }
}
