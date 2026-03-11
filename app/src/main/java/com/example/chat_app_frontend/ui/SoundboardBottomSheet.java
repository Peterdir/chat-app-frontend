package com.example.chat_app_frontend.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundboardBottomSheet extends BottomSheetDialogFragment {

    private String channelName;

    public SoundboardBottomSheet(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_soundboard_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvStandard = view.findViewById(R.id.rv_soundboard_standard);
        RecyclerView rvPremium = view.findViewById(R.id.rv_soundboard_premium);

        // Grid Layout Manager with 3 columns
        rvStandard.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvPremium.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Mock data matching the screenshot
        List<SoundItem> standardSounds = Arrays.asList(
                new SoundItem("quack", "🦆"),
                new SoundItem("airhorn", "🔊"),
                new SoundItem("cricket", "🦗"),
                new SoundItem("golf clap", "👏"),
                new SoundItem("sad horn", "🎺"),
                new SoundItem("ba dum tss", "🥁"));

        List<SoundItem> premiumSounds = Arrays.asList(
                new SoundItem("Tieng-cuoi-hehe", "👴"),
                new SoundItem("Lo-cc", "😎"),
                new SoundItem("thg nao co tien", "🤑"),
                new SoundItem("Bo may nhin", "🟩"),
                new SoundItem("Xao-cho", "🐱"),
                new SoundItem("Ditmecuocdoi", "👴"),
                new SoundItem("cuoi cc", "😇"));

        SoundboardAdapter stdAdapter = new SoundboardAdapter(getContext(), standardSounds, item -> {
            Toast.makeText(getContext(), "Phát âm thanh: " + item.name, Toast.LENGTH_SHORT).show();
        });
        rvStandard.setAdapter(stdAdapter);

        SoundboardAdapter premAdapter = new SoundboardAdapter(getContext(), premiumSounds, item -> {
            Toast.makeText(getContext(), "Yêu cầu Nitro: " + item.name, Toast.LENGTH_SHORT).show();
        });
        rvPremium.setAdapter(premAdapter);
    }

    // Static Data Class
    public static class SoundItem {
        String name;
        String emoji;

        public SoundItem(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }
    }

    // Static Adapter Wrapper
    public static class SoundboardAdapter extends RecyclerView.Adapter<SoundboardAdapter.ViewHolder> {
        private Context context;
        private List<SoundItem> items;
        private OnSoundClickListener listener;

        public interface OnSoundClickListener {
            void onSoundClick(SoundItem item);
        }

        public SoundboardAdapter(Context context, List<SoundItem> items, OnSoundClickListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_soundboard_grid, parent, false);
            // Add margin for grid spacing
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) v.getLayoutParams();
            int margin = (int) (4 * context.getResources().getDisplayMetrics().density);
            lp.setMargins(margin, margin, margin, margin);
            v.setLayoutParams(lp);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SoundItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvEmoji.setText(item.emoji);

            holder.itemView.setOnClickListener(v -> listener.onSoundClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji;
            TextView tvName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_sound_emoji);
                tvName = itemView.findViewById(R.id.tv_sound_name);
            }
        }
    }
}
