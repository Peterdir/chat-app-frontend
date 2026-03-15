package com.example.chat_app_frontend.model;

public class Decoration {
    public enum Type { NONE, STORE, REGULAR }

    private String id;
    private String name;
    private int drawableResId;
    private String description;
    private boolean isNitro;
    private Type type;
    private boolean isLocked;
    private boolean isNew;

    public Decoration(String id, String name, int drawableResId, String description, boolean isNitro, Type type) {
        this.id = id;
        this.name = name;
        this.drawableResId = drawableResId;
        this.description = description;
        this.isNitro = isNitro;
        this.type = type;
        this.isLocked = false;
        this.isNew = false;
    }

    public Decoration(String id, String name, int drawableResId, String description, boolean isNitro, Type type, boolean isLocked, boolean isNew) {
        this(id, name, drawableResId, description, isNitro, type);
        this.isLocked = isLocked;
        this.isNew = isNew;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getDrawableResId() { return drawableResId; }
    public String getDescription() { return description; }
    public boolean isNitro() { return isNitro; }
    public Type getType() { return type; }
    public boolean isLocked() { return isLocked; }
    public boolean isNew() { return isNew; }
}
