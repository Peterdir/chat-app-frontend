package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.utils.NitroEligibility;

import java.util.List;

public class DecorationAdapter extends RecyclerView.Adapter<DecorationAdapter.ViewHolder> {

    public interface OnDecorationClickListener {
        void onDecorationClick(Decoration decoration);
    }

    private List<Decoration> decorations;
    private OnDecorationClickListener listener;
    private User user;
    private int selectedPosition = -1;

    public DecorationAdapter(List<Decoration> decorations, OnDecorationClickListener listener) {
        this(decorations, listener, null);
    }

    public DecorationAdapter(List<Decoration> decorations, OnDecorationClickListener listener, User user) {
        this.decorations = decorations;
        this.listener = listener;
        this.user = user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_decoration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Decoration decoration = decorations.get(position);
        
        // Reset visibility
        holder.imgDecoration.setVisibility(View.GONE);
        holder.imgNone.setVisibility(View.GONE);
        holder.imgStoreIcon.setVisibility(View.GONE);
        holder.txtName.setVisibility(View.GONE);
        holder.imgNitro.setVisibility(View.GONE);
        holder.imgLock.setVisibility(View.GONE);
        holder.badgeNew.setVisibility(View.GONE);
        holder.avatarPlaceholder.setVisibility(View.GONE);

        switch (decoration.getType()) {
            case NONE:
                holder.imgNone.setVisibility(View.VISIBLE);
                holder.txtName.setVisibility(View.VISIBLE);
                holder.txtName.setText(decoration.getName());
                break;
            case STORE:
                holder.imgStoreIcon.setVisibility(View.VISIBLE);
                holder.txtName.setVisibility(View.VISIBLE);
                holder.txtName.setText(decoration.getName());
                if (decoration.isNew()) holder.badgeNew.setVisibility(View.VISIBLE);
                break;
            case REGULAR:
                holder.imgDecoration.setVisibility(View.VISIBLE);
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(decoration.getDrawableResId())
                    .into(holder.imgDecoration);
                holder.avatarPlaceholder.setVisibility(View.VISIBLE);
                if (decoration.isNitro()) holder.imgNitro.setVisibility(View.VISIBLE);
                boolean nitroUnlocked = !decoration.isNitro()
                        || (user != null && NitroEligibility.hasBasicOrFull(user));
                if (!nitroUnlocked) {
                    holder.imgLock.setVisibility(View.VISIBLE);
                }
                break;
        }
        
        holder.itemView.setSelected(selectedPosition == position);
        holder.selectedOutline.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onDecorationClick(decoration);
        });
    }

    @Override
    public int getItemCount() {
        return decorations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDecoration, imgNone, imgStoreIcon, imgNitro, imgLock;
        TextView txtName, badgeNew;
        View selectedOutline, avatarPlaceholder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDecoration = itemView.findViewById(R.id.img_decoration);
            imgNone = itemView.findViewById(R.id.img_none);
            imgStoreIcon = itemView.findViewById(R.id.img_store_icon);
            imgNitro = itemView.findViewById(R.id.img_nitro);
            imgLock = itemView.findViewById(R.id.img_lock);
            txtName = itemView.findViewById(R.id.txt_item_name);
            badgeNew = itemView.findViewById(R.id.badge_new);
            selectedOutline = itemView.findViewById(R.id.selected_outline);
            avatarPlaceholder = itemView.findViewById(R.id.avatar_placeholder);
        }
    }
}
