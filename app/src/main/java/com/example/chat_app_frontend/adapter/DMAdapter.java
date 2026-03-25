package com.example.chat_app_frontend.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.ui.DMChatActivity;

import java.util.List;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.DMViewHolder> {

    private List<Friend> friendList;

    public DMAdapter(List<Friend> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public DMViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm_friend, parent, false);
        return new DMViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DMViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.tvFriendName.setText(friend.getName());
        holder.tvFriendStatus.setText(friend.getStatus());

        if (friend.getAvatarResId() != 0) {
            holder.imgFriendAvatar.setImageResource(friend.getAvatarResId());
            holder.imgFriendAvatar.setVisibility(View.VISIBLE);
            holder.tvFriendInitial.setVisibility(View.GONE);
        } else {
            holder.imgFriendAvatar.setVisibility(View.GONE);
            holder.tvFriendInitial.setVisibility(View.VISIBLE);

            String name = friend.getName();
            if (name != null && !name.isEmpty()) {
                holder.tvFriendInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else {
                holder.tvFriendInitial.setText("?");
            }
        }

        // Open DM chat on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DMChatActivity.class);
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_NAME, friend.getName());
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_STATUS, friend.getStatus());
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_AVATAR, friend.getAvatarResId());
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, friend.getUid());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class DMViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        TextView tvFriendStatus;
        ImageView imgFriendAvatar;
        TextView tvFriendInitial;

        public DMViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tv_friend_name);
            tvFriendStatus = itemView.findViewById(R.id.tv_friend_status);
            imgFriendAvatar = itemView.findViewById(R.id.img_friend_avatar);
            tvFriendInitial = itemView.findViewById(R.id.tv_friend_initial);
        }
    }
}
