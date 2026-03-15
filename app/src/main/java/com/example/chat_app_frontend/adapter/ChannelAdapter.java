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
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.ui.VoiceChannelActivity;
import com.example.chat_app_frontend.ui.VoiceChannelPreviewBottomSheet;
import com.example.chat_app_frontend.ui.ServerChatActivity;
import com.example.chat_app_frontend.ui.ChannelOptionsBottomSheet;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private List<Channel> channelList;
    private Context context;
    private String serverName;
    private String serverId;

    public ChannelAdapter(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /** Thay thế toàn bộ danh sách channel và refresh. */
    public void updateChannels(List<Channel> newChannels) {
        channelList.clear();
        channelList.addAll(newChannels);
        notifyDataSetChanged();
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

        // Hide/Show logic based on VoiceStateManager
        VoiceStateManager stateManager = VoiceStateManager.getInstance();
        if ("voice".equals(channel.getType()) && channel.getName().equals(stateManager.getConnectedChannelName())) {
            holder.llNormalState.setVisibility(View.GONE);
            holder.llConnectedState.setVisibility(View.VISIBLE);

            holder.tvConnectedChannelName.setText(channel.getName());
            holder.tvConnectedUserName.setText("Duy");

            // Set up user status
            String currentStatus = stateManager.getCurrentActivityStatus();
            if (currentStatus != null && !currentStatus.isEmpty()) {
                holder.ivConnectedActivity.setVisibility(View.VISIBLE);

                int iconResId = R.drawable.ic_controller; // Default
                switch (currentStatus.toLowerCase()) {
                    case "đang chill":
                        iconResId = R.drawable.ic_status_chill;
                        break;
                    case "đang học":
                        iconResId = R.drawable.ic_status_study;
                        break;
                    case "sẽ trở lại ngay":
                        iconResId = R.drawable.ic_status_flight;
                        break;
                    case "đang xem linh tinh":
                        iconResId = R.drawable.ic_status_mailbox;
                        break;
                }
                holder.ivConnectedActivity.setImageResource(iconResId);
            } else {
                holder.ivConnectedActivity.setVisibility(View.GONE);
            }

            // Sync Mute Icon
            if (stateManager.isMuted()) {
                holder.ivConnectedMute.setVisibility(View.VISIBLE);
            } else {
                holder.ivConnectedMute.setVisibility(View.GONE);
            }

            // Sync Video Icon
            if (stateManager.isVideoOn()) {
                holder.ivConnectedVideo.setVisibility(View.VISIBLE);
            } else {
                holder.ivConnectedVideo.setVisibility(View.GONE);
            }

            // Sync Speaking Border
            if (stateManager.isSpeaking()) {
                holder.vSpeakingBorder.setVisibility(View.VISIBLE);
            } else {
                holder.vSpeakingBorder.setVisibility(View.GONE);
            }

            // Setup timer
            long joinTime = stateManager.getJoinTimeMillis();
            if (joinTime > 0) {
                holder.timerConnected
                        .setBase(android.os.SystemClock.elapsedRealtime() - (System.currentTimeMillis() - joinTime));
                holder.timerConnected.start();
            } else {
                holder.timerConnected.stop();
            }

            // Clicking connected state opens VoiceChannelActivity directly
            holder.llConnectedState.setOnClickListener(v -> {
                Intent intent = new Intent(context, VoiceChannelActivity.class);
                intent.putExtra("CHANNEL_NAME", channel.getName());
                context.startActivity(intent);
            });

        } else {
            holder.llConnectedState.setVisibility(View.GONE);
            holder.llNormalState.setVisibility(View.VISIBLE);
            holder.timerConnected.stop();

            // Lắng nghe sự kiện click mở Preview
            holder.llNormalState.setOnClickListener(v -> {
                if ("voice".equals(channel.getType())) {
                    if (context instanceof AppCompatActivity) {
                        VoiceChannelPreviewBottomSheet bottomSheet = new VoiceChannelPreviewBottomSheet(
                                channel.getName());
                        bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "VoicePreview");
                    }
                } else {
                    // Only text channels open a chat screen
                    Intent intent = new Intent(context, ServerChatActivity.class);
                    intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_ID, channel.getId());
                    intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_NAME, channel.getName());
                    intent.putExtra(ServerChatActivity.EXTRA_SERVER_ID, serverId != null ? serverId : "");
                    intent.putExtra(ServerChatActivity.EXTRA_SERVER_NAME, serverName != null ? serverName : "");
                    context.startActivity(intent);
                }
            });

            // Lắng nghe sự kiện đè lâu (Long Click) để mở BottomSheet tùy chỉnh
            holder.llNormalState.setOnLongClickListener(v -> {
                if (context instanceof AppCompatActivity) {
                    ChannelOptionsBottomSheet bottomSheet = ChannelOptionsBottomSheet.newInstance(channel.getName());
                    bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "ChannelOptions");
                    return true;
                }
                return false;
            });
        }

        // Cũng áp dụng Long Click cho trạng thái đang kết nối (Connected State)
        holder.llConnectedState.setOnLongClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                ChannelOptionsBottomSheet bottomSheet = ChannelOptionsBottomSheet.newInstance(channel.getName());
                bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "ChannelOptions");
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    private final VoiceStateManager.VoiceStateListener stateListener = new VoiceStateManager.VoiceStateListener() {
        @Override
        public void onVoiceStateChanged() {
            notifyDataSetChanged();
        }
    };

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        VoiceStateManager.getInstance().addListener(stateListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        VoiceStateManager.getInstance().removeListener(stateListener);
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder {
        View llNormalState;
        TextView tvChannelName;

        View llConnectedState;
        TextView tvConnectedChannelName;
        android.widget.Chronometer timerConnected;
        TextView tvConnectedUserName;
        android.widget.ImageView ivConnectedMute;
        android.widget.ImageView ivConnectedActivity;
        android.widget.ImageView ivConnectedVideo;
        View vSpeakingBorder;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            llNormalState = itemView.findViewById(R.id.ll_normal_state);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);

            llConnectedState = itemView.findViewById(R.id.ll_connected_state);
            tvConnectedChannelName = itemView.findViewById(R.id.tv_connected_channel_name);
            timerConnected = itemView.findViewById(R.id.timer_connected);
            tvConnectedUserName = itemView.findViewById(R.id.tv_connected_user_name);
            ivConnectedMute = itemView.findViewById(R.id.iv_connected_mute);
            ivConnectedActivity = itemView.findViewById(R.id.iv_connected_activity);
            ivConnectedVideo = itemView.findViewById(R.id.iv_connected_video);
            vSpeakingBorder = itemView.findViewById(R.id.v_speaking_border);
        }
    }
}
