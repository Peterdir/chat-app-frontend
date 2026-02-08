package com.example.chat_app_frontend.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.chat_app_frontend.R;
import java.util.ArrayList;
import java.util.List;

public class NitroActivity extends AppCompatActivity {

    // Khai báo biến cho Slider
    private ViewPager2 viewPagerPerks;
    private LinearLayout layoutDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_nitro);

        setupAnimations();

        setupSlider();
    }

    private void setupAnimations() {
        // Load các file animation từ XML
        Animation animHover = AnimationUtils.loadAnimation(this, R.anim.anim_hover);
        Animation animSlideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in);
        Animation animGlow = AnimationUtils.loadAnimation(this, R.anim.anim_glow);
        Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        Animation animClick = AnimationUtils.loadAnimation(this, R.anim.anim_click);

        findViewById(android.R.id.content).startAnimation(animSlideUp);

        ImageView imgNitro = findViewById(R.id.img_wumpus_nitro);
        if (imgNitro != null) {
            imgNitro.startAnimation(animHover);
        }

        ImageView imgBasic = findViewById(R.id.img_wumpus_basic);
        if (imgBasic != null) {
            animSlideIn.setStartOffset(1000);
            imgBasic.startAnimation(animSlideIn);
        }

        TextView tvTitle = findViewById(R.id.tv_header_nitro);
        if (tvTitle != null) {
            tvTitle.startAnimation(animGlow);
        }

        Button btnGetNitro = findViewById(R.id.btn_get_nitro);
        if (btnGetNitro != null) {
            btnGetNitro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(animClick);
                }
            });
        }

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void setupSlider() {
        viewPagerPerks = findViewById(R.id.viewPagerPerks);
        layoutDots = findViewById(R.id.layout_dots);

        List<PerkItem> list = new ArrayList<>();
        list.add(new PerkItem("Cường điệu, giễu cợt và tạo meme bằng emoji tùy chỉnh.", R.drawable.banner_emojis));
        list.add(new PerkItem("Tải lên tập tin khổng lồ 500MB để chia sẻ dữ liệu.", R.drawable.wumpus_jetpack));
        list.add(new PerkItem("Phát trực tiếp chất lượng HD mượt mà sắc nét.", R.drawable.wumpus_scooter));

        PerkAdapter adapter = new PerkAdapter(list);
        viewPagerPerks.setAdapter(adapter);

        addDots(list.size());
        updateDots(0);

        viewPagerPerks.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void addDots(int count) {
        if (layoutDots == null) return;
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            params.setMargins(10, 0, 10, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(android.R.drawable.radiobutton_off_background);
            layoutDots.addView(dot);
        }
    }

    private void updateDots(int position) {
        if (layoutDots == null) return;
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            if (i == position) {
                dot.setAlpha(1.0f);
                dot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            } else {
                dot.setAlpha(0.4f);
                dot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            }
        }
    }

    static class PerkItem {
        String title;
        int imageRes;

        public PerkItem(String title, int imageRes) {
            this.title = title;
            this.imageRes = imageRes;
        }
    }

    class PerkAdapter extends RecyclerView.Adapter<PerkAdapter.PerkViewHolder> {
        private List<PerkItem> items;

        public PerkAdapter(List<PerkItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public PerkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nitro_perk, parent, false);
            return new PerkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PerkViewHolder holder, int position) {
            PerkItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.imgIcon.setImageResource(item.imageRes);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class PerkViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView imgIcon;

            public PerkViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_perk_title);
                imgIcon = itemView.findViewById(R.id.img_perk_icon);
            }
        }
    }
}