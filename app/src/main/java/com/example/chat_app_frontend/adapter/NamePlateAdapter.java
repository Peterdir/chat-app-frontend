package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.NamePlate;

import java.util.List;

public class NamePlateAdapter extends RecyclerView.Adapter<NamePlateAdapter.ViewHolder> {

    private List<NamePlate> namePlates;
    private String selectedId;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NamePlate plate);
    }

    public NamePlateAdapter(List<NamePlate> namePlates, String selectedId, OnItemClickListener listener) {
        this.namePlates = namePlates;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_plate_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NamePlate plate = namePlates.get(position);
        
        // Reset visibility
        holder.imgPlate.setVisibility(View.VISIBLE);
        holder.placeholderContainer.setVisibility(View.GONE);
        holder.overlay.setVisibility(View.GONE);
        holder.imgLock.setVisibility(View.GONE);
        holder.imgNitro.setVisibility(View.GONE);
        holder.txtNewBadge.setVisibility(View.GONE);
        holder.viewSelection.setVisibility(plate.getId().equals(selectedId) ? View.VISIBLE : View.GONE);

        if (plate.getType() == NamePlate.Type.NONE) {
            holder.imgPlate.setVisibility(View.GONE);
            holder.placeholderContainer.setVisibility(View.VISIBLE);
            holder.imgIcon.setImageResource(R.drawable.ic_clear);
            holder.txtLabel.setText("Không");
        } else if (plate.getType() == NamePlate.Type.STORE) {
            holder.imgPlate.setVisibility(View.GONE);
            holder.placeholderContainer.setVisibility(View.VISIBLE);
            holder.imgIcon.setImageResource(R.drawable.ic_store);
            holder.txtLabel.setText("Cửa hàng");
            if (plate.isNew()) holder.txtNewBadge.setVisibility(View.VISIBLE);
        } else {
            // Regular plate item
            holder.imgPlate.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(plate.getDrawableResId())
                .into(holder.imgPlate);
            
            if (plate.isLocked()) {
                holder.overlay.setVisibility(View.VISIBLE);
                holder.imgLock.setVisibility(View.VISIBLE);
            }
            if (plate.isNitro()) {
                holder.imgNitro.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            selectedId = plate.getId();
            notifyDataSetChanged();
            if (listener != null) listener.onItemClick(plate);
        });
    }

    @Override
    public int getItemCount() {
        return namePlates.size();
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlate, imgIcon, imgLock, imgNitro;
        TextView txtLabel, txtNewBadge;
        View viewSelection, overlay, placeholderContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlate = itemView.findViewById(R.id.img_plate);
            imgIcon = itemView.findViewById(R.id.img_icon);
            imgLock = itemView.findViewById(R.id.img_lock);
            imgNitro = itemView.findViewById(R.id.img_nitro);
            txtLabel = itemView.findViewById(R.id.txt_label);
            txtNewBadge = itemView.findViewById(R.id.txt_new_badge);
            viewSelection = itemView.findViewById(R.id.view_selection);
            overlay = itemView.findViewById(R.id.overlay);
            placeholderContainer = itemView.findViewById(R.id.placeholder_container);
        }
    }
}
