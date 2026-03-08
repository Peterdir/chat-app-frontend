package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {

    private static final int[] IMAGES = {
            R.drawable.img_gameconsole,
            R.drawable.img_online_meeting2,
            R.drawable.img_online_meeting1
    };

    private static final int[] TITLES = {
            R.string.onboard_title_1,
            R.string.onboard_title_2,
            R.string.onboard_title_3
    };

    private static final int[] DESCRIPTIONS = {
            R.string.onboard_desc_1,
            R.string.onboard_desc_2,
            R.string.onboard_desc_3
    };

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.ivImage.setImageResource(IMAGES[position]);
        holder.tvTitle.setText(TITLES[position]);
        holder.tvDescription.setText(DESCRIPTIONS[position]);
    }

    @Override
    public int getItemCount() {
        return IMAGES.length;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImage;
        final TextView tvTitle;
        final TextView tvDescription;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage       = itemView.findViewById(R.id.iv_slide_image);
            tvTitle       = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}
