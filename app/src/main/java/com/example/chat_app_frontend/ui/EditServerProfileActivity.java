package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;
import com.google.android.material.tabs.TabLayout;

public class EditServerProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView tvSave;
    private View btnGetNitro;
    private View shimmerNitro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server_profile);

        initViews();
        setupTabs();
        setupButtons();
        
        // Bắt đầu hiệu ứng lấp lánh cho nút Nitro
        startShimmerAnimation();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        btnBack = findViewById(R.id.btn_back);
        tvSave = findViewById(R.id.tv_save);
        btnGetNitro = findViewById(R.id.btn_get_nitro);
        shimmerNitro = findViewById(R.id.shimmer_nitro);
    }

    private void setupTabs() {
        // Mặc định chọn tab thứ 2: Hồ Sơ Theo Máy Chủ (index 1)
        TabLayout.Tab targetTab = tabLayout.getTabAt(1);
        if (targetTab != null) {
            targetTab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Chuyển sang màn hình EditProfileActivity (Hồ Sơ Chính)
                    Intent intent = new Intent(EditServerProfileActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                    finish(); // Kết thúc màn hình hiện tại để tránh chồng chất
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        
        if (btnGetNitro != null) {
            btnGetNitro.setOnClickListener(v -> {
                Intent intent = new Intent(this, NitroActivity.class);
                startActivity(intent);
            });
        }
    }

    private void startShimmerAnimation() {
        if (btnGetNitro != null && shimmerNitro != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDestroyed() || isFinishing()) return;

                    shimmerNitro.setVisibility(View.VISIBLE);
                    shimmerNitro.setTranslationX(-150f);

                    // Lấy độ rộng của nút sau khi đã layout xong
                    btnGetNitro.post(() -> {
                        float endX = btnGetNitro.getWidth() + 150f;
                        shimmerNitro.animate()
                                .translationX(endX)
                                .setDuration(1500)
                                .withEndAction(() -> {
                                    shimmerNitro.setVisibility(View.INVISIBLE);
                                    shimmerNitro.setTranslationX(-150f);
                                    shimmerNitro.postDelayed(this, 3000); // Lặp lại sau 3 giây
                                })
                                .start();
                    });
                }
            };
            shimmerNitro.postDelayed(shimmerRunnable, 1000);
        }
    }
}
