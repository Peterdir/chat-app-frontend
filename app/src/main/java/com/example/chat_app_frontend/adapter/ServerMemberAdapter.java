package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.User;

import java.util.ArrayList;
import java.util.List;

public class ServerMemberAdapter extends RecyclerView.Adapter<ServerMemberAdapter.ViewHolder> {

    private final List<User> members = new ArrayList<>();

    public void submitList(List<User> data) {
        members.clear();
        if (data != null) {
            members.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_server_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = members.get(position);
        String display = user.getDisplayNameOrUserName();
        if (display == null || display.trim().isEmpty()) {
            display = "Unknown";
        }

        holder.tvName.setText(display);

        String userName = user.getUserName();
        holder.tvSubtitle.setText(userName != null && !userName.trim().isEmpty()
                ? "@" + userName
                : "");

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            holder.imgAvatar.setVisibility(View.VISIBLE);
            holder.tvInitial.setVisibility(View.GONE);
            Glide.with(holder.imgAvatar)
                    .load(avatarUrl)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setVisibility(View.GONE);
            holder.tvInitial.setVisibility(View.VISIBLE);
            holder.tvInitial.setText(display.isEmpty()
                    ? "?"
                    : String.valueOf(display.charAt(0)).toUpperCase());

            int idx = (user.getFirebaseUid() != null ? user.getFirebaseUid().hashCode() : 0)
                    & 0x7FFFFFFF;
            int[] palette = {
                    0xFF5865F2,
                    0xFF23A559,
                    0xFFEB459E,
                    0xFFFEE75C,
                    0xFF57F287,
                    0xFFED4245,
            };
            holder.cvAvatar.setCardBackgroundColor(palette[idx % palette.length]);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cvAvatar;
        ImageView imgAvatar;
        TextView tvInitial;
        TextView tvName;
        TextView tvSubtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvAvatar = itemView.findViewById(R.id.cv_member_avatar);
            imgAvatar = itemView.findViewById(R.id.img_member_avatar);
            tvInitial = itemView.findViewById(R.id.tv_member_initial);
            tvName = itemView.findViewById(R.id.tv_member_name);
            tvSubtitle = itemView.findViewById(R.id.tv_member_subtitle);
        }
    }
}
