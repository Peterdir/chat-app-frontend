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
import com.example.chat_app_frontend.model.Server;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {

    private List<Server> serverList;
    private OnServerClickListener listener;
    private OnServerLongClickListener longClickListener;

    public interface OnServerClickListener {
        void onServerClick(Server server); // Hàm sẽ được gọi khi có user click
    }

    public interface OnServerLongClickListener {
        void onServerLongClick(Server server);
    }

    public ServerAdapter(List<Server> serverList, OnServerClickListener listener, OnServerLongClickListener longClickListener) {
        this.serverList = serverList;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server_rail, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        Server server = serverList.get(position);
        holder.bind(server, listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgServerIcon;
        TextView tvServerInitial;
        View indicator;
        CardView cardView;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgServerIcon = itemView.findViewById(R.id.img_server_icon);
            tvServerInitial = itemView.findViewById(R.id.tv_server_initial);
            indicator = itemView.findViewById(R.id.indicator);
            cardView = itemView.findViewById(R.id.card_server_icon);
        }

        public void bind(final Server server, final OnServerClickListener listener, final OnServerLongClickListener longClickListener) {
            // Set icon or initial
            if (server.getIconResId() != 0) {
                // Local drawable (e.g. DM home item)
                imgServerIcon.setImageResource(server.getIconResId());
                imgServerIcon.setVisibility(View.VISIBLE);
                tvServerInitial.setVisibility(View.GONE);
            } else if (server.getIconUrl() != null && !server.getIconUrl().isEmpty()) {
                // Remote icon URL — load with Glide
                imgServerIcon.setVisibility(View.VISIBLE);
                tvServerInitial.setVisibility(View.GONE);
                Glide.with(imgServerIcon.getContext())
                        .load(server.getIconUrl())
                        .centerCrop()
                        .into(imgServerIcon);
            } else {
                // No icon — show first letter as initial
                imgServerIcon.setVisibility(View.GONE);
                tvServerInitial.setVisibility(View.VISIBLE);
                tvServerInitial.setText(String.valueOf(server.getName().charAt(0)).toUpperCase());
            }

            // Animation values
            float targetRadius = server.isSelected() ? 16f : 48f; // Rounded rect vs Circle
            float currentRadius = cardView.getRadius();

            // Only animate if there is a significant change to avoid layout trashing on
            // scroll
            if (Math.abs(currentRadius - dpToPx(targetRadius)) > 1f) {
                android.animation.ValueAnimator radiusAnim = android.animation.ValueAnimator.ofFloat(currentRadius,
                        dpToPx(targetRadius));
                radiusAnim.setDuration(200);
                radiusAnim.addUpdateListener(animation -> cardView.setRadius((float) animation.getAnimatedValue()));
                radiusAnim.start();
            } else {
                cardView.setRadius(dpToPx(targetRadius));
            }

            // Indicator Animation (Scale/Visibility)
            // We can animate height or visibility. Simple fade/scale is easier.
            if (server.isSelected()) {
                if (indicator.getVisibility() != View.VISIBLE) {
                    indicator.setVisibility(View.VISIBLE);
                    indicator.setScaleY(0f);
                    indicator.animate().scaleY(1f).setDuration(200).start();
                }
            } else {
                if (indicator.getVisibility() == View.VISIBLE) {
                    indicator.animate().scaleY(0f).setDuration(200)
                            .withEndAction(() -> indicator.setVisibility(View.INVISIBLE)).start();
                }
            }

            itemView.setOnClickListener(v -> listener.onServerClick(server));
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onServerLongClick(server);
                    return true;
                }
                return false;
            });
        }


        private float dpToPx(float dp) {
            return android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    itemView.getResources().getDisplayMetrics());
        }
    }

    public void setSelectedServer(String serverId) {
        int oldPosition = -1;
        int newPosition = -1;

        for (int i = 0; i < serverList.size(); i++) {
            Server server = serverList.get(i);
            if (server.isSelected()) {
                oldPosition = i;
                server.setSelected(false);
            }
            if (server.getId().equals(serverId)) {
                newPosition = i;
                server.setSelected(true);
            }
        }

        if (oldPosition != -1)
            notifyItemChanged(oldPosition);
        if (newPosition != -1)
            notifyItemChanged(newPosition);
    }

    public Server getSelectedServer() {
        for (Server server : serverList) {
            if (server.isSelected()) {
                return server;
            }
        }
        return null;
    }

    /** Thêm server mới vào cuối danh sách (không phải DM item). */
    public void addServer(Server server) {
        serverList.add(server);
        notifyItemInserted(serverList.size() - 1);
    }

    /**
     * Xóa tất cả server thật (giữ lại item DM ở vị trí 0).
     * Dùng khi cần reload danh sách từ Firebase.
     */
    public void clearRealServers() {
        int originalSize = serverList.size();
        if (originalSize > 1) {
            serverList.subList(1, originalSize).clear();
            notifyItemRangeRemoved(1, originalSize - 1);
        }
    }
}
