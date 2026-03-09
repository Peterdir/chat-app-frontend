package com.example.chat_app_frontend.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_edit_profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        tabMainProfile.setOnClickListener(v -> selectTab(true));
        tabServerProfile.setOnClickListener(v -> selectTab(false));
    }

    private void selectTab(boolean isMain) {
        // Update indicators with slide animation
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
        ((android.widget.TextView) tabMainText).setTextColor(isMain ? activeColor : inactiveColor);
        ((android.widget.TextView) tabServerText).setTextColor(isMain ? inactiveColor : activeColor);

        // Bold active tab
        ((android.widget.TextView) tabMainText).setTypeface(null,
                isMain ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        ((android.widget.TextView) tabServerText).setTypeface(null,
                isMain ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

        // Animate content switch with slide
        View showing = isMain ? tabContentMain : tabContentServer;
        View hiding = isMain ? tabContentServer : tabContentMain;
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

    private void setupButtons() {
        // Back button with rotation micro-animation
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            v.animate()
                    .rotation(-90f)
                    .alpha(0.5f)
                    .setDuration(200)
                    .withEndAction(() -> finish())
                    .start();
        });

        // Save button with pulse effect
        View btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            v.animate()
                    .scaleX(1.2f).scaleY(1.2f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(120)
                                .withEndAction(() -> finish())
                                .start();
                    }).start();
        });

        // Clear name button with spin
        ImageView btnClearName = findViewById(R.id.btn_clear_name);
        EditText etDisplayName = findViewById(R.id.et_display_name);
        if (btnClearName != null && etDisplayName != null) {
            btnClearName.setOnClickListener(v -> {
                v.animate().rotation(v.getRotation() + 180f).setDuration(250).start();
                etDisplayName.setText("");
                etDisplayName.requestFocus();
            });
        }

        // Nitro preview button native animation handled by stateListAnimator in XML
        View btnNitroPreview = findViewById(R.id.btn_nitro_preview);
        View shimmerNitroPreview = findViewById(R.id.shimmer_nitro_preview);
        startShimmerAnimation(btnNitroPreview, shimmerNitroPreview);

        View btnNitroBuy = findViewById(R.id.btn_nitro_buy);
        View shimmerNitroBuy = findViewById(R.id.shimmer_nitro_buy);
        startShimmerAnimation(btnNitroBuy, shimmerNitroBuy);

        // Item card press animations
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
                    if (isDestroyed() || isFinishing())
                        return;

                    shimmerView.setVisibility(View.VISIBLE);
                    shimmerView.setTranslationX(-100f);

                    float endX = button.getWidth() + 100f;

                    shimmerView.animate()
                            .translationX(endX)
                            .setDuration(1200)
                            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
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
        root.setAlpha(0f);
        root.setTranslationY(30f);

        // Staggered entrance for form elements
        int[] formIds = {
                R.id.label_display_name, R.id.et_display_name,
                R.id.label_pronouns, R.id.et_pronouns,
                R.id.label_about_me, R.id.et_about_me,
                R.id.item_avatar_decoration, R.id.item_profile_effect,
                R.id.item_name_plate, R.id.card_nitro_preview
        };

        // Hide all form elements initially
        for (int id : formIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(20f);
            }
        }

        // Animate root in
        root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    // Stagger form elements in
                    for (int i = 0; i < formIds.length; i++) {
                        View v = findViewById(formIds[i]);
                        if (v != null) {
                            v.animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(250)
                                    .setStartDelay(i * 40L)
                                    .setInterpolator(new DecelerateInterpolator(1.5f))
                                    .start();
                        }
                    }
                })
                .start();
    }

    @Override
    public void finish() {
        View root = findViewById(R.id.root_edit_profile);
        if (root != null) {
            root.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        super.finish();
                        overridePendingTransition(0, 0);
                    }).start();
        } else {
            super.finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
