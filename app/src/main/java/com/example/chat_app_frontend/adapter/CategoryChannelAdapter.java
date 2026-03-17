package com.example.chat_app_frontend.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.manager.VoiceStateManager;
import com.example.chat_app_frontend.model.Category;
import com.example.chat_app_frontend.model.CategoryWithChannels;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.ui.ChannelOptionsBottomSheet;
import com.example.chat_app_frontend.ui.ServerChatActivity;
import com.example.chat_app_frontend.ui.VoiceChannelActivity;
import com.example.chat_app_frontend.ui.VoiceChannelPreviewBottomSheet;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter để hiển thị categories và channels được nhóm theo category.
 */
public class CategoryChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY_HEADER = 0;
    private static final int TYPE_CHANNEL = 1;
    private static final int TYPE_UNCATEGORIZED_HEADER = 2;

    private List<Object> items; // Mix of CategoryWithChannels and Channel
    private Context context;
    private String serverId;
    private String serverName;
    private OnAddChannelClickListener onAddChannelClickListener;

    public interface OnAddChannelClickListener {
        void onAddChannelClick(Category category);
    }

    public CategoryChannelAdapter(List<CategoryWithChannels> categoriesWithChannels, List<Channel> uncategorizedChannels) {
        this.items = new ArrayList<>();
        buildItemList(categoriesWithChannels, uncategorizedChannels);
    }

    private void buildItemList(List<CategoryWithChannels> categoriesWithChannels, List<Channel> uncategorizedChannels) {
        items.clear();

        // Add categories with their channels
        for (CategoryWithChannels cwc : categoriesWithChannels) {
            items.add(cwc); // Category header
            items.addAll(cwc.getChannels()); // Channels under this category
        }

        // Add uncategorized channels
        if (!uncategorizedChannels.isEmpty()) {
            Category noneCategory = new Category("none_header", "");
            CategoryWithChannels noneGroup = new CategoryWithChannels(noneCategory);
            items.add(noneGroup);
            items.addAll(uncategorizedChannels);
        }
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setOnAddChannelClickListener(OnAddChannelClickListener listener) {
        this.onAddChannelClickListener = listener;
    }

    public void updateData(List<CategoryWithChannels> categoriesWithChannels, List<Channel> uncategorizedChannels) {
        buildItemList(categoriesWithChannels, uncategorizedChannels);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof CategoryWithChannels) {
            return TYPE_CATEGORY_HEADER;
        }
        return TYPE_CHANNEL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == TYPE_CATEGORY_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_header, parent, false);
            return new CategoryHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
            return new ChannelViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryHeaderViewHolder) {
            CategoryHeaderViewHolder categoryHolder = (CategoryHeaderViewHolder) holder;
            CategoryWithChannels cwc = (CategoryWithChannels) items.get(position);
            categoryHolder.bind(cwc.getCategory());
        } else if (holder instanceof ChannelViewHolder) {
            ChannelViewHolder channelHolder = (ChannelViewHolder) holder;
            Channel channel = (Channel) items.get(position);
            channelHolder.bind(channel);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ===== VIEW HOLDERS =====

    public class CategoryHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private ImageView btnAddChannel;

        public CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            btnAddChannel = itemView.findViewById(R.id.btn_add_channel);
        }

        public void bind(Category category) {
            if ("none_header".equals(category.getId())) {
                tvCategoryName.setVisibility(View.GONE);
                btnAddChannel.setVisibility(View.GONE);
            } else {
                tvCategoryName.setVisibility(View.VISIBLE);
                tvCategoryName.setText(category.getName().toUpperCase());
                btnAddChannel.setVisibility(View.VISIBLE);
                btnAddChannel.setOnClickListener(v -> {
                    if (onAddChannelClickListener != null) {
                        onAddChannelClickListener.onAddChannelClick(category);
                    }
                });
            }
        }
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llNormalState, llConnectedState;
        private TextView tvChannelIcon, tvChannelName, tvConnectedChannelName, tvConnectedUserName;
        private ImageView ivConnectedActivity, ivConnectedMute, ivConnectedVideo;
        private View vSpeakingBorder;
        private android.widget.Chronometer timerConnected;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            llNormalState = itemView.findViewById(R.id.ll_normal_state);
            llConnectedState = itemView.findViewById(R.id.ll_connected_state);
            tvChannelIcon = itemView.findViewById(R.id.tv_channel_icon);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);

            // Connected state views
            tvConnectedChannelName = itemView.findViewById(R.id.tv_connected_channel_name);
            tvConnectedUserName = itemView.findViewById(R.id.tv_connected_user_name);
            ivConnectedActivity = itemView.findViewById(R.id.iv_connected_activity);
            ivConnectedMute = itemView.findViewById(R.id.iv_connected_mute);
            ivConnectedVideo = itemView.findViewById(R.id.iv_connected_video);
            vSpeakingBorder = itemView.findViewById(R.id.v_speaking_border);
            timerConnected = itemView.findViewById(R.id.timer_connected);
        }

        public void bind(Channel channel) {
            // Cập nhật icon và tên kênh
            if ("voice".equals(channel.getType())) {
                tvChannelIcon.setText("🔊");
            } else {
                tvChannelIcon.setText("#");
            }
            tvChannelName.setText(channel.getName());

            // Hide/Show logic based on VoiceStateManager
            VoiceStateManager stateManager = VoiceStateManager.getInstance();
            if ("voice".equals(channel.getType()) && channel.getName().equals(stateManager.getConnectedChannelName())) {
                llNormalState.setVisibility(View.GONE);
                llConnectedState.setVisibility(View.VISIBLE);

                tvConnectedChannelName.setText(channel.getName());
                tvConnectedUserName.setText("Duy");

                // Set up user status
                String currentStatus = stateManager.getCurrentActivityStatus();
                if (currentStatus != null && !currentStatus.isEmpty()) {
                    ivConnectedActivity.setVisibility(View.VISIBLE);

                    int iconResId = R.drawable.ic_controller;
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
                    ivConnectedActivity.setImageResource(iconResId);
                } else {
                    ivConnectedActivity.setVisibility(View.GONE);
                }

                // Sync Mute Icon
                if (stateManager.isMuted()) {
                    ivConnectedMute.setVisibility(View.VISIBLE);
                } else {
                    ivConnectedMute.setVisibility(View.GONE);
                }

                // Sync Video Icon
                if (stateManager.isVideoOn()) {
                    ivConnectedVideo.setVisibility(View.VISIBLE);
                } else {
                    ivConnectedVideo.setVisibility(View.GONE);
                }

                // Sync Speaking Border
                if (stateManager.isSpeaking()) {
                    vSpeakingBorder.setVisibility(View.VISIBLE);
                } else {
                    vSpeakingBorder.setVisibility(View.GONE);
                }

                // Setup timer
                long joinTime = stateManager.getJoinTimeMillis();
                if (joinTime > 0) {
                    timerConnected.setBase(android.os.SystemClock.elapsedRealtime() - (System.currentTimeMillis() - joinTime));
                    timerConnected.start();
                } else {
                    timerConnected.stop();
                }

                // Clicking connected state opens VoiceChannelActivity directly
                llConnectedState.setOnClickListener(v -> {
                    Intent intent = new Intent(context, VoiceChannelActivity.class);
                    intent.putExtra("CHANNEL_NAME", channel.getName());
                    context.startActivity(intent);
                });

            } else {
                llConnectedState.setVisibility(View.GONE);
                llNormalState.setVisibility(View.VISIBLE);
                timerConnected.stop();

                // Lắng nghe sự kiện click mở Preview
                llNormalState.setOnClickListener(v -> {
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
                llNormalState.setOnLongClickListener(v -> {
                    if (context instanceof AppCompatActivity) {
                        ChannelOptionsBottomSheet bottomSheet = ChannelOptionsBottomSheet.newInstance(channel.getName());
                        bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "ChannelOptions");
                        return true;
                    }
                    return false;
                });
            }

            // Cũng áp dụng Long Click cho trạng thái đang kết nối (Connected State)
            llConnectedState.setOnLongClickListener(v -> {
                if (context instanceof AppCompatActivity) {
                    ChannelOptionsBottomSheet bottomSheet = ChannelOptionsBottomSheet.newInstance(channel.getName());
                    bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "ChannelOptions");
                    return true;
                }
                return false;
            });
        }
    }
}
