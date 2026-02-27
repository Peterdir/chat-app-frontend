package com.example.chat_app_frontend.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.ui.VoiceChannelActivity;
import com.example.chat_app_frontend.ui.VoiceChannelPreviewBottomSheet;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private List<Channel> channelList;
    private Context context;

    public ChannelAdapter(List<Channel> channelList) {
        this.channelList = channelList;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channelList.get(position);

        // Cập nhật tên kênh (thêm ký hiệu # cho text và cho voice cho đẹp)
        if ("voice".equals(channel.getType())) {
            holder.tvChannelName.setText("\uD83D\uDD0A " + channel.getName());
        } else {
            holder.tvChannelName.setText("# " + channel.getName());
        }

        // --- Sự kiện click mở kênh Voice ---
        holder.itemView.setOnClickListener(v -> {
            if ("voice".equals(channel.getType())) {
                // Hiển thị Bottom Sheet thay vì vào thẳng Activity
                if (context instanceof AppCompatActivity) {
                    VoiceChannelPreviewBottomSheet bottomSheet = new VoiceChannelPreviewBottomSheet(channel.getName());
                    bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "VoicePreview");
                }
            } else {
                // Xử lý khi nhấn vào text channel (Tạm thời in Toast)
                Toast.makeText(context, "Chưa hỗ trợ Text Chat: " + channel.getName(), Toast.LENGTH_SHORT).show();
            }
        });
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
