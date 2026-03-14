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
import com.example.chat_app_frontend.model.FriendRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter dùng chung cho cả 2 tab:
 *   - "Chờ xác nhận": hiện nút Chấp nhận + Từ chối
 *   - "Đã gửi": hiện nút Hủy
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    public enum Mode { PENDING, SENT }

    public interface OnActionListener {
        void onAccept(FriendRequest request, int position);
        void onDeclineOrCancel(FriendRequest request, int position);
    }

    private final List<FriendRequest> requests;
    private final Mode                mode;
    private final OnActionListener    listener;

    public FriendRequestAdapter(List<FriendRequest> requests,
                                 Mode mode,
                                 OnActionListener listener) {
        this.requests = requests;
        this.mode     = mode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest req = requests.get(position);

        holder.tvName.setText(req.getSenderName());
        holder.tvTimestamp.setText(formatTime(req.getTimestamp()));

        // Avatar
        if (req.getSenderAvatarUrl() != null && !req.getSenderAvatarUrl().isEmpty()) {
            Glide.with(holder.ivAvatar.getContext())
                    .load(req.getSenderAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar1);
        }

        if (mode == Mode.PENDING) {
            // Hiện nút Chấp nhận
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setText("✕");
            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(req, holder.getAdapterPosition());
            });
        } else {
            // Tab "Đã gửi" — ẩn nút Accept
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setText("Hủy");
        }

        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDeclineOrCancel(req, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return requests.size(); }

    public void removeItem(int position) {
        if (position >= 0 && position < requests.size()) {
            requests.remove(position);
            notifyItemRemoved(position);
        }
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView  tvName, tvTimestamp, btnAccept, btnDecline;

        ViewHolder(View v) {
            super(v);
            ivAvatar     = v.findViewById(R.id.iv_avatar);
            tvName       = v.findViewById(R.id.tv_name);
            tvTimestamp  = v.findViewById(R.id.tv_timestamp);
            btnAccept    = v.findViewById(R.id.btn_accept);
            btnDecline   = v.findViewById(R.id.btn_decline);
        }
    }
}
