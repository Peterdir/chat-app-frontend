package com.example.chat_app_frontend.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Full-screen custom view that renders the "digital ping" ripple effect.
 *
 * When {@link #trigger()} is called, three expanding rings radiate outward
 * from the screen center with staggered start times, simulating a sonar / ping
 * pulse. A brief bright center flash fires simultaneously.
 */
public class PingRingView extends View {

    private static final int   NUM_RINGS      = 3;
    private static final long  RING_DURATION  = 750L;
    private static final long  RING_STAGGER   = 170L;
    private static final long  FLASH_DURATION = 220L;

    private static final int COLOR_RING  = 0xFF7C5CF5; // Purple
    private static final int COLOR_FLASH = 0xFFCEC5FF; // Soft lavender-white flash

    private final float[] ringScale = new float[NUM_RINGS];
    private final float[] ringAlpha = new float[NUM_RINGS];

    private float centerFlashAlpha = 0f;

    private final Paint ringPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------
    public PingRingView(Context context) {
        super(context);
        init();
    }

    public PingRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PingRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2.5f);
        ringPaint.setColor(COLOR_RING);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(11f);
        glowPaint.setColor(COLOR_RING);

        flashPaint.setStyle(Paint.Style.FILL);
        flashPaint.setColor(COLOR_FLASH);
    }

    // ---------------------------------------------------------------------------
    // State – ping origin
    // ---------------------------------------------------------------------------
    private float pingCX = -1f;
    private float pingCY = -1f;

    // ---------------------------------------------------------------------------
    // API
    // ---------------------------------------------------------------------------

    /**
     * Fires the ping animation radiating from the given screen-coordinate point.
     * Pass the centre of the logo icon for best effect.
     */
    public void trigger(float originX, float originY) {
        this.pingCX = originX;
        this.pingCY = originY;
        animateCenterFlash();
        animateRings();
    }

    private void animateCenterFlash() {
        ValueAnimator flash = ValueAnimator.ofFloat(0f, 1f);
        flash.setDuration(FLASH_DURATION);
        flash.addUpdateListener(anim -> {
            float t = (float) anim.getAnimatedValue();
            // Ramp up then ramp down to produce a sharp flash
            centerFlashAlpha = t < 0.4f ? t / 0.4f : 1f - ((t - 0.4f) / 0.6f);
            invalidate();
        });
        flash.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                centerFlashAlpha = 0f;
                invalidate();
            }
        });
        flash.start();
    }

    private void animateRings() {
        for (int i = 0; i < NUM_RINGS; i++) {
            final int idx = i;
            ValueAnimator ring = ValueAnimator.ofFloat(0f, 1f);
            ring.setDuration(RING_DURATION);
            ring.setStartDelay(i * RING_STAGGER);
            ring.setInterpolator(new DecelerateInterpolator(1.8f));
            ring.addUpdateListener(anim -> {
                float t = (float) anim.getAnimatedValue();
                ringScale[idx] = t;
                ringAlpha[idx] = 1f - t;
                invalidate();
            });
            ring.start();
        }
    }

    // ---------------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Fall back to view centre if trigger hasn't been called yet
        float cx = (pingCX >= 0) ? pingCX : getWidth()  / 2f;
        float cy = (pingCY >= 0) ? pingCY : getHeight() / 2f;
        float maxRadius = Math.min(getWidth(), getHeight()) * 0.44f;

        // Center flash – small bright disc
        if (centerFlashAlpha > 0f) {
            flashPaint.setAlpha((int) (centerFlashAlpha * 255));
            canvas.drawCircle(cx, cy, 18f * centerFlashAlpha, flashPaint);
        }

        // Expanding rings
        for (int i = 0; i < NUM_RINGS; i++) {
            if (ringAlpha[i] <= 0f) continue;
            float r = ringScale[i] * maxRadius;

            glowPaint.setAlpha((int) (ringAlpha[i] * 55));
            canvas.drawCircle(cx, cy, r, glowPaint);

            ringPaint.setAlpha((int) (ringAlpha[i] * 210));
            canvas.drawCircle(cx, cy, r, ringPaint);
        }
    }
}
