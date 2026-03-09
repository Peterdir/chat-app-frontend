package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.SettingItem;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SettingItem> items;

    public SettingsAdapter(List<SettingItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SettingItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_row, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText(item.title);
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).tvTitle.setText(item.title);
            if (item.iconRes != 0) {
                ((ItemViewHolder) holder).imgIcon.setImageResource(item.iconRes);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_section_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgIcon;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgIcon = itemView.findViewById(R.id.img_icon);
        }
    }
}