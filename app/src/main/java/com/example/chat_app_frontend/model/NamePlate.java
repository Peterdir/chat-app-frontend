package com.example.chat_app_frontend.model;

public class NamePlate {
    public enum Type { NONE, STORE, REGULAR }

    private String id;
    private String name;
    private int drawableResId;
    private String description;
    private String receivedDate;
    private boolean isNitro;
    private Type type;
    private boolean isLocked;
    private boolean isNew;

    public NamePlate(String id, String name, int drawableResId, String description, String receivedDate, boolean isNitro, Type type) {
        this.id = id;
        this.name = name;
        this.drawableResId = drawableResId;
        this.description = description;
        this.receivedDate = receivedDate;
        this.isNitro = isNitro;
        this.type = type;
        this.isLocked = false;
        this.isNew = false;
    }

    public NamePlate(String id, String name, int drawableResId, String description, String receivedDate, boolean isNitro, Type type, boolean isLocked, boolean isNew) {
        this(id, name, drawableResId, description, receivedDate, isNitro, type);
        this.isLocked = isLocked;
        this.isNew = isNew;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getDrawableResId() { return drawableResId; }
    public String getDescription() { return description; }
    public String getReceivedDate() { return receivedDate; }
    public boolean isNitro() { return isNitro; }
    public Type getType() { return type; }
    public boolean isLocked() { return isLocked; }
    public boolean isNew() { return isNew; }
}
