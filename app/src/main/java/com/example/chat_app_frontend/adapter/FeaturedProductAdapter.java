package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.ShopItem;
import java.util.List;

public class FeaturedProductAdapter extends RecyclerView.Adapter<FeaturedProductAdapter.FeaturedViewHolder> {
    private List<ShopItem> items;

    public FeaturedProductAdapter(List<ShopItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_product, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        ShopItem item = items.get(position);
        holder.tvName.setText(item.getName());
        if (item.getImageResId() != 0) {
            holder.ivImage.setImageResource(item.getImageResId());
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivFeaturedImage);
            tvName = itemView.findViewById(R.id.tvFeaturedName);
        }
    }
}
