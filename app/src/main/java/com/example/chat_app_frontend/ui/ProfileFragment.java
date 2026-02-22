package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chat_app_frontend.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        setupCloseImproveCard(view);
        setupEditProfileButton(view);
        animateProfileEntrance(view);

        return view;
    }

    private void setupEditProfileButton(View view) {
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }).start();
            });
        }
    }

    private void setupCloseImproveCard(View view) {
        View cardImproveProfile = view.findViewById(R.id.card_improve_profile);
        View btnClose = view.findViewById(R.id.btn_close_improve);

        btnClose.setOnClickListener(v -> {
            // Animate card disappearing
            cardImproveProfile.animate()
                    .alpha(0f)
                    .translationY(-30f)
                    .setDuration(250)
                    .withEndAction(() -> cardImproveProfile.setVisibility(View.GONE))
                    .start();
        });

        // Setup Header Buttons with Click Animations
        setupHeaderButton(view, R.id.btn_nitro_header);
        setupHeaderButton(view, R.id.btn_store_header);
        setupHeaderButton(view, R.id.btn_settings);

        // Start Shimmer Effect for Nitro Button
        startShimmerAnimation(view);
    }

    private void startShimmerAnimation(View view) {
        View shimmerView = view.findViewById(R.id.shimmer_view);
        View nitroButton = view.findViewById(R.id.btn_nitro_header);

        if (shimmerView != null && nitroButton != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (getContext() == null)
                        return; // Fragment detached

                    shimmerView.setVisibility(View.VISIBLE);
                    shimmerView.setTranslationX(-100f);

                    // Width of the button approx 80-100dp
                    float endX = nitroButton.getWidth() + 100f;

                    shimmerView.animate()
                            .translationX(endX)
                            .setDuration(1200) // Slow glide
                            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                            .withEndAction(() -> {
                                shimmerView.setVisibility(View.INVISIBLE);
                                shimmerView.setTranslationX(-100f);
                                shimmerView.postDelayed(this, 3000); // Repeat every 3s
                            })
                            .start();
                }
            };
            // Initial delay
            shimmerView.postDelayed(shimmerRunnable, 1000);
        }
    }

    private void setupHeaderButton(View view, int buttonId) {
        View button = view.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }).start();
            });
        }
    }

    private void animateProfileEntrance(View view) {
        // Avatar Decoration Rotation
        View avatarDecoration = view.findViewById(R.id.avatar_decoration);
        if (avatarDecoration != null) {
            Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_indefinitely);
            avatarDecoration.startAnimation(rotate);
        }

        // Avatar bounce animation
        View avatarContainer = view.findViewById(R.id.avatar_container);
        Animation scaleBounce = AnimationUtils.loadAnimation(getContext(), R.anim.scale_bounce);
        scaleBounce.setStartOffset(200);
        avatarContainer.startAnimation(scaleBounce);

        // Staggered slide-up animation for each card
        int[] cardIds = {
                R.id.username_section,
                R.id.btn_edit_profile,
                R.id.card_improve_profile,
                R.id.card_orbs,
                R.id.card_joined,
                R.id.card_friends,
                R.id.card_notes
        };

        for (int i = 0; i < cardIds.length; i++) {
            View card = view.findViewById(cardIds[i]);
            if (card != null) {
                card.setAlpha(0f);
                card.setTranslationY(60f);
                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(150 + (i * 80L)) // staggered delay
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }
        }

        // Status dot pulse animation
        View statusDot = view.findViewById(R.id.status_dot);
        if (statusDot != null) {
            statusDot.setScaleX(0f);
            statusDot.setScaleY(0f);
            statusDot.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setStartDelay(600)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                    .start();
        }
    }
}
