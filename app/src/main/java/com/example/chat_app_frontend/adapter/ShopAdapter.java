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

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    private List<ShopItem> shopItems;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }
    
    public ShopAdapter(List<ShopItem> shopItems, OnItemClickListener listener) {
        this.shopItems = shopItems;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_product, parent, false);
        return new ShopViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        ShopItem item = shopItems.get(position);
        holder.bind(item, listener);
    }
    
    @Override
    public int getItemCount() {
        return shopItems.size();
    }
    
    public void updateList(List<ShopItem> newList) {
        this.shopItems = newList;
        notifyDataSetChanged();
    }
    
    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameText;
        private TextView priceText;
        
        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameText = itemView.findViewById(R.id.nameText);
            priceText = itemView.findViewById(R.id.priceText);
        }
        
        public void bind(ShopItem item, OnItemClickListener listener) {
            nameText.setText(item.getName());
            
            // Set price
            if (item.getPrice() != null && !item.getPrice().equals("0")) {
                priceText.setText(item.getPrice());
                priceText.setVisibility(View.VISIBLE);
            } else {
                priceText.setVisibility(View.VISIBLE);
                priceText.setText("Free");
            }
            
            // Load image from resource ID
            if (item.getImageResId() != 0) {
                imageView.setImageResource(item.getImageResId());
                // Match the style from Shop layout
                if ("banner".equals(item.getType())) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder_product);
            }
            
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
