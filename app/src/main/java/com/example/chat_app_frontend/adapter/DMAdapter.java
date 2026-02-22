package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Friend;

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
            // Default: Show Initial on Background
            holder.imgFriendAvatar.setVisibility(View.GONE);
            holder.tvFriendInitial.setVisibility(View.VISIBLE);

            // Get first letter
            String name = friend.getName();
            if (name != null && !name.isEmpty()) {
                holder.tvFriendInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else {
                holder.tvFriendInitial.setText("?");
            }

            // Pick a random discord color for background (simulated by CardView background
            // in XML or here)
            // For now, let's keep the default green from XML or cycle through if needed.
            // Valid improvement: Set CardView background color programmatically based on
            // name hash.
        }
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
