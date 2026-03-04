package com.example.chat_app_frontend.model;

public class ShopItem {
    private String id;
    private String name;
    private int imageResId; // Đổi từ String sang int
    private String price;
    private String type; // "circular" or "banner"
    
    public ShopItem(String id, String name, int imageResId, String price, String type) {
        this.id = id;
        this.name = name;
        this.imageResId = imageResId;
        this.price = price;
        this.type = type;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
    public String getPrice() { return price; }
    public String getType() { return type; }
}
