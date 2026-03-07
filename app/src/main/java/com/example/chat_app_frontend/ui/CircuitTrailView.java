package com.example.chat_app_frontend.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Full-screen custom view that renders the animated circuit-board trail effect.
 *
 * Circuit traces are defined in absolute coordinates relative to the screen center
 * (0, 0 = screen center). As the logo slides from left to right, each circuit
 * segment "lights up" once the logo passes it, creating the trailing glow effect.
 *
 * When the logo has not yet passed a segment, a very faint static version of the
 * trace is drawn to contribute to the dark "textured" background feel.
 */
public class CircuitTrailView extends View {

    // ---------------------------------------------------------------------------
    // Circuit trace data
    // Each trace is a series of (x, y) waypoints connected by straight segments.
    // Coordinates are relative to screen center; negative X = left of center.
    // ---------------------------------------------------------------------------
    // Icon is centered at (0, -55) relative to screen center (icon above wordmark).
    // All traces converge toward that point from the left edge.
    private static final int[][][] TRACES = {
        // Trace A – main mid-level highway → icon center
        {{-260, -55}, {-190, -55}, {-190, -95}, {-110, -95}, {-110, -55}, {-44, -55}},
        // Trace B – upper accent route
        {{-245, -120}, {-170, -120}, {-170, -150}, {-90, -150}, {-90, -120}, {-25, -120}},
        // Trace C – lower route → below icon, then up
        {{-250, 15}, {-175, 15}, {-175, -30}, {-100, -30}, {-100, 15}, {-32, 15}},
        // Trace D – cross-connector: A ↔ B (vertical bridge)
        {{-190, -95}, {-190, -120}},
        // Trace E – cross-connector: A ↔ C
        {{-175, -55}, {-175, 15}},
        // Trace F – far-left vertical spine spanning all routes
        {{-260, -55}, {-260, -120}},
        // Trace G – accent branch merging B ↔ A at mid-section
        {{-110, -95}, {-110, -120}, {-90, -120}},
        // Trace H – lower branch mirroring G
        {{-100, -55}, {-100, 15}},
        // Trace I – extra upper-left wing (texture / depth)
        {{-245, -120}, {-245, -155}, {-195, -155}},
        // Trace J – lower-right terminal stub reaching toward icon base
        {{-44, -55}, {-44, 15}, {-32, 15}},
    };

    // Accent traces (cyan) — Trace B (upper route) and Trace G (B↔A branch)
    private static final int[] ACCENT_TRACE_INDICES = {1, 6, 8};

    // ---------------------------------------------------------------------------
    // Paint objects
    // ---------------------------------------------------------------------------
    private static final int COLOR_PRIMARY     = 0xFF7C5CF5; // Electric purple
    private static final int COLOR_ACCENT      = 0xFF00D4FF; // Cyan
    private static final int COLOR_FAINT_PRI   = 0x127C5CF5; // Very faint purple (static bg)
    private static final int COLOR_FAINT_ACC   = 0x1200D4FF; // Very faint cyan

    private final Paint primaryPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentGlow    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint faintPrimary  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint faintAccent   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---------------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------------
    /** translationX of the logoGroup view (−screenWidth → 0). */
    private float logoTranslationX = Float.NEGATIVE_INFINITY;
    private boolean frozen = false;

    // ---------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------
    public CircuitTrailView(Context context) {
        super(context);
        init();
    }

    public CircuitTrailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircuitTrailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        primaryPaint.setStyle(Paint.Style.STROKE);
        primaryPaint.setStrokeCap(Paint.Cap.ROUND);
        primaryPaint.setColor(COLOR_PRIMARY);
        primaryPaint.setStrokeWidth(2.4f);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setColor(COLOR_PRIMARY);
        glowPaint.setStrokeWidth(9f);

        accentPaint.setStyle(Paint.Style.STROKE);
        accentPaint.setStrokeCap(Paint.Cap.ROUND);
        accentPaint.setColor(COLOR_ACCENT);
        accentPaint.setStrokeWidth(2f);

        accentGlow.setStyle(Paint.Style.STROKE);
        accentGlow.setStrokeCap(Paint.Cap.ROUND);
        accentGlow.setColor(COLOR_ACCENT);
        accentGlow.setStrokeWidth(8f);

        faintPrimary.setStyle(Paint.Style.STROKE);
        faintPrimary.setStrokeCap(Paint.Cap.ROUND);
        faintPrimary.setColor(COLOR_FAINT_PRI);
        faintPrimary.setStrokeWidth(1.5f);

        faintAccent.setStyle(Paint.Style.STROKE);
        faintAccent.setStrokeCap(Paint.Cap.ROUND);
        faintAccent.setColor(COLOR_FAINT_ACC);
        faintAccent.setStrokeWidth(1.5f);

        dotPaint.setStyle(Paint.Style.FILL);
    }

    // ---------------------------------------------------------------------------
    // API called from SplashActivity
    // ---------------------------------------------------------------------------

    /**
     * Updates the trail position. Called on every frame of the slide animation.
     *
     * @param translationX current translationX of the logo group (−screenWidth → 0)
     */
    public void updateTrail(float translationX) {
        this.logoTranslationX = translationX;
        invalidate();
    }

    /** Called when the logo finishes sliding; locks all traces as fully lit. */
    public void freezeTrail() {
        frozen = true;
        logoTranslationX = 0f;
        invalidate();
    }

    // ---------------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() == 0 || getHeight() == 0) return;

        float screenCX = getWidth()  / 2f;
        float screenCY = getHeight() / 2f;

        // Logo's current absolute X on screen
        float logoScreenX = frozen
                ? screenCX
                : (screenCX + logoTranslationX);

        for (int t = 0; t < TRACES.length; t++) {
            int[][] trace  = TRACES[t];
            boolean accent = isAccent(t);

            for (int i = 1; i < trace.length; i++) {
                float x1 = screenCX + trace[i - 1][0];
                float y1 = screenCY + trace[i - 1][1];
                float x2 = screenCX + trace[i][0];
                float y2 = screenCY + trace[i][1];

                float segMidX = (x1 + x2) / 2f;
                boolean lit   = logoScreenX > segMidX;

                if (lit) {
                    drawLitSegment(canvas, x1, y1, x2, y2, accent, x1);
                } else {
                    drawFaintSegment(canvas, x1, y1, x2, y2, accent);
                }
            }
        }
    }

    private void drawLitSegment(Canvas canvas,
                                float x1, float y1, float x2, float y2,
                                boolean accent, float leftEdgeX) {
        // Edge-fade: gradually reveal near the left screen boundary
        float edgeFade = Math.min(1f, Math.max(0f, leftEdgeX / 80f));

        if (accent) {
            accentGlow.setAlpha((int)(80 * edgeFade));
            canvas.drawLine(x1, y1, x2, y2, accentGlow);
            accentPaint.setAlpha((int)(230 * edgeFade));
            canvas.drawLine(x1, y1, x2, y2, accentPaint);
            dotPaint.setColor(COLOR_ACCENT);
        } else {
            glowPaint.setAlpha((int)(55 * edgeFade));
            canvas.drawLine(x1, y1, x2, y2, glowPaint);
            primaryPaint.setAlpha((int)(230 * edgeFade));
            canvas.drawLine(x1, y1, x2, y2, primaryPaint);
            dotPaint.setColor(COLOR_PRIMARY);
        }

        // Junction dot at start point
        dotPaint.setAlpha((int)(230 * edgeFade));
        canvas.drawCircle(x1, y1, accent ? 2.5f : 3.5f, dotPaint);
    }

    private void drawFaintSegment(Canvas canvas,
                                  float x1, float y1, float x2, float y2,
                                  boolean accent) {
        if (accent) {
            canvas.drawLine(x1, y1, x2, y2, faintAccent);
        } else {
            canvas.drawLine(x1, y1, x2, y2, faintPrimary);
        }
    }

    private boolean isAccent(int traceIndex) {
        for (int idx : ACCENT_TRACE_INDICES) {
            if (idx == traceIndex) return true;
        }
        return false;
    }
}
