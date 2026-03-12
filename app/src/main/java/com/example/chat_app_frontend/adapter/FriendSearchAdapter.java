package com.example.chat_app_frontend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.User;

import java.util.List;

/**
 * Adapter cho màn hình tìm kiếm bạn bè.
 * Mỗi item hiển thị avatar, tên, username và trạng thái nút "Thêm/Đã gửi/Bạn bè".
 */
public class FriendSearchAdapter extends RecyclerView.Adapter<FriendSearchAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAddFriend(User user, int position);
    }

    private final List<User>       users;
    private final List<String>     statuses; // "none" | "sent" | "friend"
    private final OnActionListener listener;

    public FriendSearchAdapter(List<User> users, List<String> statuses, OnActionListener listener) {
        this.users    = users;
        this.statuses = statuses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user   = users.get(position);
        String status = statuses.size() > position ? statuses.get(position) : "none";

        holder.tvDisplayName.setText(user.getDisplayNameOrUserName());
        holder.tvUsername.setText("@" + (user.getUserName() != null ? user.getUserName() : ""));

        // Avatar: dùng Glide nếu có URL, fallback sang drawable
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.ivAvatar.getContext())
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar1);
        }

        // Nút thêm bạn theo trạng thái
        Context ctx = holder.itemView.getContext();
        switch (status) {
            case "friend":
                holder.btnAdd.setText("Bạn bè");
                holder.btnAdd.setEnabled(false);
                holder.btnAdd.setAlpha(0.5f);
                break;
            case "sent":
                holder.btnAdd.setText("Đã gửi");
                holder.btnAdd.setEnabled(false);
                holder.btnAdd.setAlpha(0.5f);
                break;
            default: // "none"
                holder.btnAdd.setText("Thêm Bạn");
                holder.btnAdd.setEnabled(true);
                holder.btnAdd.setAlpha(1f);
                holder.btnAdd.setOnClickListener(v -> {
                    if (listener != null) listener.onAddFriend(user, holder.getAdapterPosition());
                });
                break;
        }
    }

    @Override
    public int getItemCount() { return users.size(); }

    /** Cập nhật trạng thái nút tại vị trí sau khi gửi lời mời */
    public void setStatus(int position, String status) {
        if (position >= 0 && position < statuses.size()) {
            statuses.set(position, status);
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView  tvDisplayName, tvUsername, btnAdd;

        ViewHolder(View v) {
            super(v);
            ivAvatar      = v.findViewById(R.id.iv_avatar);
            tvDisplayName = v.findViewById(R.id.tv_display_name);
            tvUsername    = v.findViewById(R.id.tv_username);
            btnAdd        = v.findViewById(R.id.btn_add_friend);
        }
    }
}
