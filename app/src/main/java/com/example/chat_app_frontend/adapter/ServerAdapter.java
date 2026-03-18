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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {

    private List<Server> serverList;
    private OnServerClickListener listener;
    private OnServerLongClickListener longClickListener;

    public interface OnServerClickListener {
        void onServerClick(Server server);
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

        // Thêm 2 biến này để lắng nghe Real-time
        private DatabaseReference serverRef;
        private ValueEventListener serverListener;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgServerIcon = itemView.findViewById(R.id.img_server_icon);
            tvServerInitial = itemView.findViewById(R.id.tv_server_initial);
            indicator = itemView.findViewById(R.id.indicator);
            cardView = itemView.findViewById(R.id.card_server_icon);
        }

        public void bind(final Server server, final OnServerClickListener listener, final OnServerLongClickListener longClickListener) {

            if (serverRef != null && serverListener != null) {
                serverRef.removeEventListener(serverListener);
            }

            updateIconUI(server);

            if (server.getIconResId() == 0 && server.getId() != null && !server.getId().isEmpty()) {
                serverRef = FirebaseDatabase.getInstance().getReference("servers").child(server.getId());
                serverListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String newIconUrl = snapshot.child("iconUrl").getValue(String.class);
                            String newName = snapshot.child("name").getValue(String.class);

                            server.setIconUrl(newIconUrl != null ? newIconUrl : "");
                            if (newName != null) server.setName(newName);

                            updateIconUI(server);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                };
                serverRef.addValueEventListener(serverListener);
            }

            float targetRadius = server.isSelected() ? 16f : 48f;
            float currentRadius = cardView.getRadius();

            if (Math.abs(currentRadius - dpToPx(targetRadius)) > 1f) {
                android.animation.ValueAnimator radiusAnim = android.animation.ValueAnimator.ofFloat(currentRadius, dpToPx(targetRadius));
                radiusAnim.setDuration(200);
                radiusAnim.addUpdateListener(animation -> cardView.setRadius((float) animation.getAnimatedValue()));
                radiusAnim.start();
            } else {
                cardView.setRadius(dpToPx(targetRadius));
            }

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

        private void updateIconUI(Server server) {
            if (server.getIconResId() != 0) {
                // Icon cục bộ (Ví dụ nút Trang chủ DM)
                imgServerIcon.setImageResource(server.getIconResId());
                imgServerIcon.setVisibility(View.VISIBLE);
                tvServerInitial.setVisibility(View.GONE);
            } else if (server.getIconUrl() != null && !server.getIconUrl().isEmpty()) {
                // Đã có link ảnh hoặc chuỗi Base64
                imgServerIcon.setVisibility(View.VISIBLE);
                tvServerInitial.setVisibility(View.GONE);
                Glide.with(imgServerIcon.getContext())
                        .load(server.getIconUrl())
                        .centerCrop()
                        .into(imgServerIcon);
            } else {
                imgServerIcon.setVisibility(View.GONE);
                tvServerInitial.setVisibility(View.VISIBLE);
                if (server.getName() != null && !server.getName().isEmpty()) {
                    tvServerInitial.setText(String.valueOf(server.getName().charAt(0)).toUpperCase());
                }
            }
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

        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (newPosition != -1) notifyItemChanged(newPosition);
    }

    public Server getSelectedServer() {
        for (Server server : serverList) {
            if (server.isSelected()) {
                return server;
            }
        }
        return null;
    }

    public void addServer(Server server) {
        serverList.add(server);
        notifyItemInserted(serverList.size() - 1);
    }

    public void clearRealServers() {
        int originalSize = serverList.size();
        if (originalSize > 1) {
            serverList.subList(1, originalSize).clear();
            notifyItemRangeRemoved(1, originalSize - 1);
        }
    }
}