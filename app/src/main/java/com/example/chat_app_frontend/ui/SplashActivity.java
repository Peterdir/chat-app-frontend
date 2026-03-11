package com.example.chat_app_frontend.ui;

import com.example.chat_app_frontend.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;



/**
 * GamerConnect splash / intro activity.
 *
 * Animation sequence:
 *  1. (300 ms delay)  – brief pause on dark background so eyes adjust
 *  2. (0 – 900 ms)   – full logo slides from far left to screen center;
 *                       circuit board traces light up in its wake
 *  3. (900 – 1 450 ms) – "ping" ripple radiates from logo; circuit fades out
 *  4. (1 450 – 2 400 ms) – camera zooms into the controller icon (pivot at icon center);
 *                           wordmark fades away
 *  5. (2 400 – 2 750 ms) – fade to black, then launch MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Timings (ms)
    // -------------------------------------------------------------------------
    private static final long INITIAL_DELAY    = 300L;
    private static final long SLIDE_DURATION   = 900L;
    private static final long CIRCUIT_FADE_MS  = 500L;
    private static final long PING_SETTLE_MS   = 550L;
    private static final long ZOOM_DURATION    = 950L;
    private static final long TEXT_FADE_MS     = 560L;
    private static final long SCREEN_FADE_MS   = 350L;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------
    private View            logoGroup;
    private ImageView       ivControllerIcon;
    private ImageView       ivLogoWordmark;
    private CircuitTrailView circuitTrailView;
    private PingRingView    pingRingView;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoGroup        = findViewById(R.id.logo_group);
        ivControllerIcon = findViewById(R.id.iv_controller_icon);
        ivLogoWordmark   = findViewById(R.id.tv_logo_text);
        circuitTrailView = findViewById(R.id.circuit_trail_view);
        pingRingView     = findViewById(R.id.ping_ring_view);

        // Wait until views are measured before starting so we can read widths
        logoGroup.post(this::startIntroSequence);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // =========================================================================
    // Phase 1 – Slide-in
    // =========================================================================
    private void startIntroSequence() {
        float screenW = getResources().getDisplayMetrics().widthPixels;

        // Park logo far off-screen left before animation starts
        logoGroup.setTranslationX(-screenW);

        ObjectAnimator slideIn = ObjectAnimator.ofFloat(
                logoGroup, "translationX", -screenW, 0f);
        slideIn.setDuration(SLIDE_DURATION);
        slideIn.setInterpolator(new DecelerateInterpolator(2.5f));

        // Drive the circuit-trail view on every frame
        slideIn.addUpdateListener(animation -> {
            float tx = (float) animation.getAnimatedValue();
            circuitTrailView.updateTrail(tx);
        });

        slideIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onLogoArrived();
            }
        });

        handler.postDelayed(slideIn::start, INITIAL_DELAY);
    }

    // =========================================================================
    // Phase 2 – Ping ripple + circuit fade-out
    // =========================================================================
    private void onLogoArrived() {
        circuitTrailView.freezeTrail();

        // Compute icon centre in screen coordinates for the ping origin
        int[] loc = new int[2];
        ivControllerIcon.getLocationOnScreen(loc);
        float iconCX = loc[0] + ivControllerIcon.getWidth()  / 2f;
        float iconCY = loc[1] + ivControllerIcon.getHeight() / 2f;

        // Ping rings radiate from the icon centre
        pingRingView.setVisibility(View.VISIBLE);
        pingRingView.trigger(iconCX, iconCY);

        // Circuit trail fades out while ping plays
        ObjectAnimator fadeCircuit = ObjectAnimator.ofFloat(
                circuitTrailView, "alpha", 1f, 0f);
        fadeCircuit.setDuration(CIRCUIT_FADE_MS);
        fadeCircuit.start();

        // After ping settles, start the zoom
        handler.postDelayed(this::zoomIntoController, PING_SETTLE_MS);
    }

    // =========================================================================
    // Phase 3 – Slow camera zoom into controller icon
    // =========================================================================
    private void zoomIntoController() {
        // Determine the controller icon's center in screen coordinates
        int[] loc = new int[2];
        ivControllerIcon.getLocationOnScreen(loc);
        float pivotX = loc[0] + ivControllerIcon.getWidth()  / 2f;
        float pivotY = loc[1] + ivControllerIcon.getHeight() / 2f;

        // Set the zoom pivot on the decor view so the whole scene scales
        View decorView = getWindow().getDecorView();
        decorView.setPivotX(pivotX);
        decorView.setPivotY(pivotY);

        ObjectAnimator zoomX    = ObjectAnimator.ofFloat(decorView,   "scaleX",   1f, 3.8f);
        ObjectAnimator zoomY    = ObjectAnimator.ofFloat(decorView,   "scaleY",   1f, 3.8f);
        ObjectAnimator textFade = ObjectAnimator.ofFloat(ivLogoWordmark, "alpha", 1f, 0f);
        ObjectAnimator pingFade = ObjectAnimator.ofFloat(pingRingView, "alpha",   1f, 0f);

        zoomX.setDuration(ZOOM_DURATION);
        zoomY.setDuration(ZOOM_DURATION);
        textFade.setDuration(TEXT_FADE_MS);
        pingFade.setDuration(TEXT_FADE_MS);

        AccelerateDecelerateInterpolator smooth = new AccelerateDecelerateInterpolator();
        zoomX.setInterpolator(smooth);
        zoomY.setInterpolator(smooth);

        AnimatorSet zoomSet = new AnimatorSet();
        zoomSet.playTogether(zoomX, zoomY, textFade, pingFade);
        zoomSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeToBlackAndLaunch(decorView);
            }
        });

        zoomSet.start();
    }

    // =========================================================================
    // Phase 4 – Fade to black and launch main screen
    // =========================================================================
    private void fadeToBlackAndLaunch(View decorView) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(decorView, "alpha", 1f, 0f);
        fadeOut.setDuration(SCREEN_FADE_MS);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        fadeOut.start();
    }
}
