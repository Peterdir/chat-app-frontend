package com.example.chat_app_frontend.model;

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
    private int effectResId;
    private Type type;
    private NitroRequirement nitroRequirement;

    public ProfileEffect(String id, String name, String description, int thumbnailResId, int effectResId, Type type) {
        this(id, name, description, thumbnailResId, effectResId, type, NitroRequirement.NONE);
    }

    public ProfileEffect(
            String id,
            String name,
            String description,
            int thumbnailResId,
            int effectResId,
            Type type,
            NitroRequirement nitroRequirement
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailResId = thumbnailResId;
        this.effectResId = effectResId;
        this.type = type;
        this.nitroRequirement = nitroRequirement != null ? nitroRequirement : NitroRequirement.NONE;
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

    public NitroRequirement getNitroRequirement() {
        return nitroRequirement;
    }
}
