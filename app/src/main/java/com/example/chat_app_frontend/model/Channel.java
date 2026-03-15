package com.example.chat_app_frontend.model;

import java.util.HashMap;
import java.util.Map;

public class Channel {
    private String id;
    private String name;
    private String type; // "text" or "voice"
    private int position;

    // Required for Firebase deserialization
    public Channel() {}

    public Channel(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("position", position);
        return map;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
