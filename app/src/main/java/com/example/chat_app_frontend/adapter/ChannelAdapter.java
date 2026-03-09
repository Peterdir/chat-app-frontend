package com.example.chat_app_frontend.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.ui.ServerChatActivity;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private List<Channel> channelList;
    private String serverName;

    public ChannelAdapter(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channelList.get(position);
        holder.tvChannelName.setText(channel.getName());

        // Only text channels open a chat screen
        if ("text".equals(channel.getType())) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ServerChatActivity.class);
                intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_NAME, channel.getName());
                intent.putExtra(ServerChatActivity.EXTRA_SERVER_NAME, serverName != null ? serverName : "");
                v.getContext().startActivity(intent);
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView tvChannelName;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
        }
    }
}
