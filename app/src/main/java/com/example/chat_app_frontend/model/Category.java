package com.example.chat_app_frontend.model;

import java.util.HashMap;
import java.util.Map;

public class Category {
    private String id;
    private String name;
    private int position;

    // Required for Firebase deserialization
    public Category() {}

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("position", position);
        return map;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
