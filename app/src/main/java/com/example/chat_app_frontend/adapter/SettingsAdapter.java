package com.example.chat_app_frontend.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.SettingsItem;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SettingsItem item);
    }

    private List<SettingsItem> settingsItems;
    private OnItemClickListener listener;

    public SettingsAdapter(List<SettingsItem> settingsItems, OnItemClickListener listener) {
        this.settingsItems = settingsItems;
        this.listener = listener;
    }

    public SettingsAdapter(List<SettingsItem> settingsItems) {
        this(settingsItems, null);
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settings_row, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        SettingsItem item = settingsItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconRes());

        // Handle Status
        if (item.getStatus() != null && !item.getStatus().isEmpty()) {
            holder.tvStatus.setText(item.getStatus());
            holder.tvStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // Handle NEW badge
        holder.tvNewBadge.setVisibility(item.isNew() ? View.VISIBLE : View.GONE);

        // Handle Logout/Destructive style
        if (item.isDestructive()) {
            int redColor = Color.parseColor("#F23F43");
            holder.tvTitle.setTextColor(redColor);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(redColor));
            holder.ivChevron.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.id.tvTitle == R.id.tvTitle ? R.color.discord_text_primary : R.color.discord_text_primary));
            holder.ivIcon.setImageTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.discord_text_secondary)));
            holder.ivChevron.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {

        return settingsItems.size();
    }

    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivChevron;
        TextView tvTitle, tvStatus, tvNewBadge;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivChevron = itemView.findViewById(R.id.ivChevron);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewBadge = itemView.findViewById(R.id.tvNewBadge);
        }
    }
}
