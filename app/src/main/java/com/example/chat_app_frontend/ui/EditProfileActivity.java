package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.chat_app_frontend.R;

public class EditProfileActivity extends AppCompatActivity {

    private View tabMainProfile, tabServerProfile;
    private View tabMainIndicator, tabServerIndicator;
    private View tabMainText, tabServerText;
    private NestedScrollView tabContentMain, tabContentServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Apply system bar insets
        View rootLayout = findViewById(R.id.root_edit_profile);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupTabs();
        setupButtons();
        animateEntrance();
    }

    private void initViews() {
        tabMainProfile = findViewById(R.id.tab_main_profile);
        tabServerProfile = findViewById(R.id.tab_server_profile);
        tabMainIndicator = findViewById(R.id.tab_main_indicator);
        tabServerIndicator = findViewById(R.id.tab_server_indicator);
        tabMainText = findViewById(R.id.tab_main_text);
        tabServerText = findViewById(R.id.tab_server_text);
        tabContentMain = findViewById(R.id.tab_content_main);
        tabContentServer = findViewById(R.id.tab_content_server);
    }

    private void setupTabs() {
        if (tabMainProfile != null) tabMainProfile.setOnClickListener(v -> selectTab(true));
        if (tabServerProfile != null) tabServerProfile.setOnClickListener(v -> selectTab(false));
    }

    private void selectTab(boolean isMain) {
        if (tabMainIndicator == null || tabServerIndicator == null || tabMainText == null || tabServerText == null) return;

        // Update indicators with animation
        tabMainIndicator.animate().alpha(isMain ? 1f : 0f).setDuration(200).start();
        tabServerIndicator.animate().alpha(isMain ? 0f : 1f).setDuration(200).start();
        tabMainIndicator.setVisibility(View.VISIBLE);
        tabServerIndicator.setVisibility(View.VISIBLE);

        // Scale active tab text
        tabMainText.animate().scaleX(isMain ? 1.05f : 1f).scaleY(isMain ? 1.05f : 1f).setDuration(200).start();
        tabServerText.animate().scaleX(isMain ? 1f : 1.05f).scaleY(isMain ? 1f : 1.05f).setDuration(200).start();

        // Update text colors
        int activeColor = getResources().getColor(R.color.discord_text_primary, null);
        int inactiveColor = getResources().getColor(R.color.discord_text_secondary, null);
        ((TextView) tabMainText).setTextColor(isMain ? activeColor : inactiveColor);
        ((TextView) tabServerText).setTextColor(isMain ? inactiveColor : activeColor);

        // Bold active tab
        ((TextView) tabMainText).setTypeface(null,
                isMain ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        ((TextView) tabServerText).setTypeface(null,
                isMain ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

        // Animate content switch
        View showing = isMain ? tabContentMain : tabContentServer;
        View hiding = isMain ? tabContentServer : tabContentMain;
        
        if (showing != null && hiding != null) {
            float slideDir = isMain ? -1f : 1f;
            hiding.animate()
                    .alpha(0f)
                    .translationX(slideDir * 60f)
                    .setDuration(180)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        hiding.setVisibility(View.GONE);
                        hiding.setTranslationX(0f);
                        showing.setAlpha(0f);
                        showing.setTranslationX(-slideDir * 40f);
                        showing.setVisibility(View.VISIBLE);
                        showing.animate()
                                .alpha(1f)
                                .translationX(0f)
                                .setDuration(250)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    }).start();
        }
    }

    private void setupButtons() {
        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Save button
        View btnSave = findViewById(R.id.btn_save);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> finish());
        }

        // Clear name button
        View btnClearName = findViewById(R.id.btn_clear_name);
        EditText etDisplayName = findViewById(R.id.et_display_name);
        if (btnClearName != null && etDisplayName != null) {
            btnClearName.setOnClickListener(v -> etDisplayName.setText(""));
        }

        // Nitro preview
        View btnNitroPreview = findViewById(R.id.btn_nitro_preview);
        View shimmerNitroPreview = findViewById(R.id.shimmer_nitro_preview);
        if (btnNitroPreview != null) {
            startShimmerAnimation(btnNitroPreview, shimmerNitroPreview);
            btnNitroPreview.setOnClickListener(v -> {
                Intent intent = new Intent(this, NitroActivity.class);
                startActivity(intent);
            });
        }

        // Item clicks
        setupItemPress(R.id.item_avatar_decoration);
        setupItemPress(R.id.item_profile_effect);
        setupItemPress(R.id.item_name_plate);
    }

    private void setupItemPress(int viewId) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120)
                                .setInterpolator(new OvershootInterpolator(1.5f)).start())
                        .start();
            });
        }
    }

    private void startShimmerAnimation(View button, View shimmerView) {
        if (button != null && shimmerView != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDestroyed() || isFinishing()) return;

                    shimmerView.setVisibility(View.VISIBLE);
                    shimmerView.setTranslationX(-100f);

                    float endX = button.getWidth() + 100f;

                    shimmerView.animate()
                            .translationX(endX)
                            .setDuration(1200)
                            .withEndAction(() -> {
                                shimmerView.setVisibility(View.INVISIBLE);
                                shimmerView.setTranslationX(-100f);
                                shimmerView.postDelayed(this, 3000);
                            })
                            .start();
                }
            };
            shimmerView.postDelayed(shimmerRunnable, 1000);
        }
    }

    private void animateEntrance() {
        View root = findViewById(R.id.root_edit_profile);
        if (root == null) return;

        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(300).start();

        int[] formIds = {
                R.id.label_display_name, R.id.et_display_name,
                R.id.label_pronouns, R.id.label_about_me, R.id.et_about_me,
                R.id.item_avatar_decoration, R.id.item_profile_effect,
                R.id.item_name_plate, R.id.card_nitro_preview
        };

        for (int i = 0; i < formIds.length; i++) {
            View v = findViewById(formIds[i]);
            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(20f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(250)
                        .setStartDelay(100 + i * 40L)
                        .start();
            }
        }
    }
}