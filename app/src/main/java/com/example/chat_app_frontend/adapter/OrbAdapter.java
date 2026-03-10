package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.OrbItem;
import java.util.List;

public class OrbAdapter extends RecyclerView.Adapter<OrbAdapter.OrbViewHolder> {

    private List<OrbItem> orbItems;

    public OrbAdapter(List<OrbItem> orbItems) {
        this.orbItems = orbItems;
    }

    @NonNull
    @Override
    public OrbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orb_card, parent, false);
        return new OrbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrbViewHolder holder, int position) {
        OrbItem item = orbItems.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvItemPrice.setText(String.valueOf(item.getPrice()));
        
        if (item.getImageResId() != 0) {
            holder.imgAsset.setImageResource(item.getImageResId());
        } else {
            // Placeholder color if no image
            holder.imgAsset.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return orbItems.size();
    }

    public static class OrbViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemPrice;
        ImageView imgAsset;

        public OrbViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            imgAsset = itemView.findViewById(R.id.imgAsset);
        }
    }
}
