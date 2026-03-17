package com.example.chat_app_frontend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a category with its channels for display purposes.
 */
public class CategoryWithChannels {
    private Category category;
    private List<Channel> channels;

    public CategoryWithChannels(Category category) {
        this.category = category;
        this.channels = new ArrayList<>();
    }

    public Category getCategory() {
        return category;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
}
