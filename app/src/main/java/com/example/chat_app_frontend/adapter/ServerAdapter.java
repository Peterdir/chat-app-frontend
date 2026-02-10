package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Server;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {

    private List<Server> serverList;
    private OnServerClickListener listener;

    public interface OnServerClickListener {
        void onServerClick(Server server);
    }

    public ServerAdapter(List<Server> serverList, OnServerClickListener listener) {
        this.serverList = serverList;
        this.listener = listener;
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
        holder.bind(server, listener);
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

        public void bind(final Server server, final OnServerClickListener listener) {
            // Set icon or initial
            if (server.getIconResId() != 0) {
                imgServerIcon.setImageResource(server.getIconResId());
                imgServerIcon.setVisibility(View.VISIBLE);
                tvServerInitial.setVisibility(View.GONE);
            } else {
                imgServerIcon.setVisibility(View.GONE);
                tvServerInitial.setVisibility(View.VISIBLE);
                tvServerInitial.setText(String.valueOf(server.getName().charAt(0)));
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
}
