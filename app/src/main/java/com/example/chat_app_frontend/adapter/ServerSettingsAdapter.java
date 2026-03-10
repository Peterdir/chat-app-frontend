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
<<<<<<< HEAD:app/src/main/java/com/example/chat_app_frontend/adapter/ServerSettingsAdapter.java
import com.example.chat_app_frontend.model.ServerSettingItem;
import java.util.List;

public class ServerSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ServerSettingItem> items;

    public ServerSettingsAdapter(List<ServerSettingItem> items) {
        this.items = items;
=======
import com.example.chat_app_frontend.model.SettingsItem;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SettingsItem item);
>>>>>>> 98364ca20ec11ad484fb3f642dd8c536af6b168d:app/src/main/java/com/example/chat_app_frontend/adapter/SettingsAdapter.java
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
<<<<<<< HEAD:app/src/main/java/com/example/chat_app_frontend/adapter/ServerSettingsAdapter.java
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ServerSettingItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server_setting_header, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ServerSettingItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText(item.title);
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).tvTitle.setText(item.title);
            if (item.iconRes != 0) {
                ((ItemViewHolder) holder).imgIcon.setImageResource(item.iconRes);
            }
=======
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
>>>>>>> 98364ca20ec11ad484fb3f642dd8c536af6b168d:app/src/main/java/com/example/chat_app_frontend/adapter/SettingsAdapter.java
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
