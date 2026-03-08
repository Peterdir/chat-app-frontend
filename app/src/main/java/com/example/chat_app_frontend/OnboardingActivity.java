package com.example.chat_app_frontend;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chat_app_frontend.adapter.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private static final int DOT_WIDTH_ACTIVE_DP   = 24;
    private static final int DOT_WIDTH_INACTIVE_DP = 8;
    private static final int DOT_ANIM_DURATION_MS  = 220;

    private ViewPager2 viewPager;
    private View[] dots;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.view_pager);
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        viewPager.setAdapter(new OnboardingAdapter());

        // Smooth page-change callback – animate dots accordingly
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                animateDots(currentPage, position);
                currentPage = position;
            }
        });

        // Set initial dot state after first layout pass
        viewPager.post(() -> activateDot(0, false));

        // ĐĂNG KÝ → màn hình đăng ký
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ĐÃ CÓ TÀI KHOẢN → màn hình đăng nhập
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    // -------------------------------------------------------------------------
    // Dot animation
    // -------------------------------------------------------------------------

    /**
     * Animates the dot for {@code fromPage} shrinking and {@code toPage} growing.
     */
    private void animateDots(int fromPage, int toPage) {
        int activePx   = dpToPx(DOT_WIDTH_ACTIVE_DP);
        int inactivePx = dpToPx(DOT_WIDTH_INACTIVE_DP);

        animateDotWidth(dots[fromPage], dots[fromPage].getWidth(), inactivePx,
                R.drawable.bg_dot_inactive, false);
        animateDotWidth(dots[toPage],   dots[toPage].getWidth(),   activePx,
                R.drawable.bg_dot_active, true);
    }

    /** Sets a dot to active or inactive immediately (no animation). */
    private void activateDot(int index, boolean animate) {
        int activePx   = dpToPx(DOT_WIDTH_ACTIVE_DP);
        int inactivePx = dpToPx(DOT_WIDTH_INACTIVE_DP);

        for (int i = 0; i < dots.length; i++) {
            boolean isActive = (i == index);
            if (animate) {
                int targetPx = isActive ? activePx : inactivePx;
                int drawableRes = isActive ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive;
                animateDotWidth(dots[i], dots[i].getWidth(), targetPx, drawableRes, isActive);
            } else {
                ViewGroup.LayoutParams lp = dots[i].getLayoutParams();
                lp.width = isActive ? activePx : inactivePx;
                dots[i].setLayoutParams(lp);
                dots[i].setBackground(ContextCompat.getDrawable(this,
                        isActive ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive));
            }
        }
    }

    private void animateDotWidth(View dot, int fromWidth, int toWidth,
                                 int drawableRes, boolean setDrawableNow) {
        if (setDrawableNow) {
            dot.setBackground(ContextCompat.getDrawable(this, drawableRes));
        }
        ValueAnimator animator = ValueAnimator.ofInt(fromWidth, toWidth);
        animator.setDuration(DOT_ANIM_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            ViewGroup.LayoutParams lp = dot.getLayoutParams();
            lp.width = (int) anim.getAnimatedValue();
            dot.setLayoutParams(lp);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!setDrawableNow) {
                    dot.setBackground(ContextCompat.getDrawable(
                            OnboardingActivity.this, drawableRes));
                }
            }
        });
        animator.start();
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    /**
     * Placeholder navigation – replace with actual Register / Login activity.
     *
     * @param isRegister true = go to register flow, false = go to login flow
     */
    private void navigateNext(boolean isRegister) {
        // TODO: Replace MainActivity.class with RegisterActivity / LoginActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("flow", isRegister ? "register" : "login");
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
