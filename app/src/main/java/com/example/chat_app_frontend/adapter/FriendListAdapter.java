package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Friend;

import java.util.List;

/** Adapter hiển thị danh sách bạn bè (tab "Bạn bè"). */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    public interface OnFriendInteractionListener {
        void onMessage(Friend friend);
        void onAvatarClicked(Friend friend);
    }

    private final List<Friend> friends;
    private final OnFriendInteractionListener listener;

    public FriendListAdapter(List<Friend> friends, OnFriendInteractionListener listener) {
        this.friends  = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend f = friends.get(position);
        holder.tvName.setText(f.getFriendName());
        holder.tvStatus.setText("Bạn bè");

        // Avatar
        if (f.getFriendAvatarUrl() != null && !f.getFriendAvatarUrl().isEmpty()) {
            Glide.with(holder.ivAvatar.getContext())
                    .load(f.getFriendAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar1);
        }

        holder.btnMessage.setOnClickListener(v -> {
            if (listener != null) listener.onMessage(f);
        });

        holder.ivAvatar.setOnClickListener(v -> {
            if (listener != null) listener.onAvatarClicked(f);
        });
        
        holder.itemView.setOnClickListener(v -> {
             if (listener != null) listener.onAvatarClicked(f);
        });
    }

    @Override
    public int getItemCount() { return friends.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, btnMessage;
        TextView  tvName, tvStatus;

        ViewHolder(View v) {
            super(v);
            ivAvatar   = v.findViewById(R.id.iv_avatar);
            tvName     = v.findViewById(R.id.tv_name);
            tvStatus   = v.findViewById(R.id.tv_status);
            btnMessage = v.findViewById(R.id.btn_message);
        }
    }
}
