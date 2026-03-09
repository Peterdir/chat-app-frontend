package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);
        holder.avatar.setImageResource(notif.getAvatarResId());
        holder.message.setText(notif.getMessage());
        holder.time.setText(notif.getTimeAgo());
        if (notif.hasButton()) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText(notif.getButtonText());
        } else {
            holder.actionButton.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView message, time, actionButton;
        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_notif_avatar);
            message = itemView.findViewById(R.id.tv_notif_message);
            time = itemView.findViewById(R.id.tv_notif_time);
            actionButton = itemView.findViewById(R.id.btn_notif_action);
        }
    }
}
