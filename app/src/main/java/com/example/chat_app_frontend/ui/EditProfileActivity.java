package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;
import com.google.android.material.tabs.TabLayout;

public class EditProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView btnSave;
    private View btnNitroPreview;
    private View shimmerNitroPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupTabs();
        setupButtons();
        
        // Bắt đầu hiệu ứng lấp lánh cho nút Nitro
        startShimmerAnimation();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnNitroPreview = findViewById(R.id.btn_nitro_preview);
        shimmerNitroPreview = findViewById(R.id.shimmer_nitro_preview);
    }

    private void setupTabs() {
        // Mặc định chọn tab thứ 1: Hồ Sơ Chính (index 0)
        TabLayout.Tab targetTab = tabLayout.getTabAt(0);
        if (targetTab != null) {
            targetTab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    // Chuyển sang màn hình EditServerProfileActivity (Hồ Sơ Theo Máy Chủ)
                    Intent intent = new Intent(EditProfileActivity.this, EditServerProfileActivity.class);
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
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> finish());
        }

        // Clear name button
        View btnClearName = findViewById(R.id.btn_clear_name);
        EditText etDisplayName = findViewById(R.id.et_display_name);
        if (btnClearName != null && etDisplayName != null) {
            btnClearName.setOnClickListener(v -> etDisplayName.setText(""));
        }

        if (btnNitroPreview != null) {
            btnNitroPreview.setOnClickListener(v -> {
                Intent intent = new Intent(this, NitroActivity.class);
                startActivity(intent);
            });
        }
    }

    private void startShimmerAnimation() {
        if (btnNitroPreview != null && shimmerNitroPreview != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDestroyed() || isFinishing()) return;

                    shimmerNitroPreview.setVisibility(View.VISIBLE);
                    shimmerNitroPreview.setTranslationX(-150f);

                    btnNitroPreview.post(() -> {
                        float endX = btnNitroPreview.getWidth() + 150f;
                        shimmerNitroPreview.animate()
                                .translationX(endX)
                                .setDuration(1500)
                                .withEndAction(() -> {
                                    shimmerNitroPreview.setVisibility(View.INVISIBLE);
                                    shimmerNitroPreview.setTranslationX(-150f);
                                    shimmerNitroPreview.postDelayed(this, 3000);
                                })
                                .start();
                    });
                }
            };
            shimmerNitroPreview.postDelayed(shimmerRunnable, 1000);
        }
    }
}
