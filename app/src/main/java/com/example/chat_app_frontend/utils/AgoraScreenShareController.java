package com.example.chat_app_frontend.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.Nullable;

import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.AgoraVideoFrame;

import java.nio.ByteBuffer;

public class AgoraScreenShareController {
    public interface Callback {
        void onScreenShareStarted(int customVideoTrackId);
        void onScreenShareStopped();
        void onScreenShareError(String userMessage, @Nullable Throwable error);
    }

    private static final String TAG = "AgoraScreenShare";
    private static final int MAX_CAPTURE_EDGE = 960;
    private static final long FRAME_INTERVAL_MS = 125L;

    private final Context appContext;
    private final MediaProjectionManager mediaProjectionManager;
    private final Callback callback;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private HandlerThread captureThread;
    private Handler captureHandler;
    private RtcEngine rtcEngine;
    private int customVideoTrackId = -1;
    private byte[] rgbaBuffer;
    private boolean isRunning = false;
    private long lastFrameSentAtMs = 0L;

    private final MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            Log.i(TAG, "MediaProjection stopped by system or user.");
            boolean shouldNotify;
            synchronized (AgoraScreenShareController.this) {
                shouldNotify = isRunning;
                stopCaptureInternal();
            }
            if (shouldNotify) {
                callback.onScreenShareStopped();
            }
        }
    };

    public AgoraScreenShareController(Context context, Callback callback) {
        this.appContext = context.getApplicationContext();
        this.callback = callback;
        this.mediaProjectionManager = (MediaProjectionManager) appContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public synchronized boolean startCapture(RtcEngine rtcEngine, int resultCode, Intent resultData) {
        if (mediaProjectionManager == null) {
            callback.onScreenShareError("Thiet bi khong ho tro MediaProjection", null);
            return false;
        }
        stopCaptureInternal();
        this.rtcEngine = rtcEngine;

        try {
            int externalSourceCode = rtcEngine.setExternalVideoSource(
                    true,
                    false,
                    Constants.ExternalVideoSourceType.VIDEO_FRAME
            );
            if (externalSourceCode != 0) {
                callback.onScreenShareError("Khong the khoi tao external video source", null);
                return false;
            }

            customVideoTrackId = rtcEngine.createCustomVideoTrack();
            if (customVideoTrackId <= 0) {
                callback.onScreenShareError("Khong tao duoc custom screen track", null);
                stopCaptureInternal();
                return false;
            }

            Size captureSize = resolveCaptureSize();

            captureThread = new HandlerThread("ScreenShareCaptureThread");
            captureThread.start();
            captureHandler = new Handler(captureThread.getLooper());

            imageReader = ImageReader.newInstance(
                    captureSize.getWidth(),
                    captureSize.getHeight(),
                    PixelFormat.RGBA_8888,
                    2
            );
            imageReader.setOnImageAvailableListener(this::handleImageAvailable, captureHandler);

            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
            if (mediaProjection == null) {
                callback.onScreenShareError("Khong lay duoc MediaProjection token", null);
                stopCaptureInternal();
                return false;
            }

            mediaProjection.registerCallback(projectionCallback, captureHandler);
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ChatAppScreenShare",
                    captureSize.getWidth(),
                    captureSize.getHeight(),
                    appContext.getResources().getDisplayMetrics().densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null,
                    captureHandler
            );
            if (virtualDisplay == null) {
                callback.onScreenShareError("Khong tao duoc VirtualDisplay", null);
                stopCaptureInternal();
                return false;
            }

            lastFrameSentAtMs = 0L;
            isRunning = true;
            callback.onScreenShareStarted(customVideoTrackId);
            return true;
        } catch (SecurityException securityException) {
            callback.onScreenShareError("Quyen chia se man hinh khong hop le", securityException);
        } catch (Exception exception) {
            callback.onScreenShareError("Khoi dong chia se man hinh that bai", exception);
        }

        stopCaptureInternal();
        return false;
    }

    public synchronized void stopCapture() {
        boolean wasRunning = isRunning || customVideoTrackId > 0;
        stopCaptureInternal();
        if (wasRunning) {
            callback.onScreenShareStopped();
        }
    }

    public synchronized int getCustomVideoTrackId() {
        return customVideoTrackId;
    }

    private void handleImageAvailable(ImageReader reader) {
        RtcEngine activeEngine;
        int activeTrackId;

        synchronized (this) {
            if (!isRunning || rtcEngine == null || customVideoTrackId <= 0) {
                Image staleImage = reader.acquireLatestImage();
                if (staleImage != null) {
                    staleImage.close();
                }
                return;
            }
            activeEngine = rtcEngine;
            activeTrackId = customVideoTrackId;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastFrameSentAtMs < FRAME_INTERVAL_MS) {
            Image skippedImage = reader.acquireLatestImage();
            if (skippedImage != null) {
                skippedImage.close();
            }
            return;
        }

        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }

        try {
            Image.Plane[] planes = image.getPlanes();
            if (planes == null || planes.length == 0) {
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int expectedSize = width * height * 4;
            if (rgbaBuffer == null || rgbaBuffer.length != expectedSize) {
                rgbaBuffer = new byte[expectedSize];
            }

            Image.Plane plane = planes[0];
            ByteBuffer buffer = plane.getBuffer();
            int rowStride = plane.getRowStride();
            int pixelStride = plane.getPixelStride();
            int destinationOffset = 0;

            if (pixelStride == 4) {
                for (int row = 0; row < height; row++) {
                    buffer.position(row * rowStride);
                    buffer.get(rgbaBuffer, destinationOffset, width * 4);
                    destinationOffset += width * 4;
                }
            } else {
                for (int row = 0; row < height; row++) {
                    int rowStart = row * rowStride;
                    for (int column = 0; column < width; column++) {
                        int sourceIndex = rowStart + (column * pixelStride);
                        rgbaBuffer[destinationOffset++] = buffer.get(sourceIndex);
                        rgbaBuffer[destinationOffset++] = buffer.get(sourceIndex + 1);
                        rgbaBuffer[destinationOffset++] = buffer.get(sourceIndex + 2);
                        rgbaBuffer[destinationOffset++] = buffer.get(sourceIndex + 3);
                    }
                }
            }

            AgoraVideoFrame frame = new AgoraVideoFrame();
            frame.format = AgoraVideoFrame.FORMAT_RGBA;
            frame.timeStamp = System.currentTimeMillis();
            frame.stride = width;
            frame.height = height;
            frame.rotation = 0;
            frame.buf = rgbaBuffer;

            int result = activeEngine.pushExternalVideoFrameById(frame, activeTrackId);
            if (result == 0) {
                lastFrameSentAtMs = now;
            } else {
                Log.w(TAG, "pushExternalVideoFrameById failed with code=" + result);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Failed to push screen frame", exception);
        } finally {
            image.close();
        }
    }

    private synchronized void stopCaptureInternal() {
        isRunning = false;
        lastFrameSentAtMs = 0L;

        if (imageReader != null) {
            imageReader.setOnImageAvailableListener(null, null);
            imageReader.close();
            imageReader = null;
        }

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            try {
                mediaProjection.unregisterCallback(projectionCallback);
            } catch (Exception ignored) {
            }
            try {
                mediaProjection.stop();
            } catch (Exception ignored) {
            }
            mediaProjection = null;
        }

        if (captureThread != null) {
            captureThread.quitSafely();
            captureThread = null;
            captureHandler = null;
        }

        if (rtcEngine != null) {
            if (customVideoTrackId > 0) {
                rtcEngine.destroyCustomVideoTrack(customVideoTrackId);
            }
            rtcEngine.setExternalVideoSource(false, false, Constants.ExternalVideoSourceType.VIDEO_FRAME);
        }

        customVideoTrackId = -1;
        rgbaBuffer = null;
    }

    private Size resolveCaptureSize() {
        int rawWidth;
        int rawHeight;

        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = windowManager.getMaximumWindowMetrics();
            rawWidth = metrics.getBounds().width();
            rawHeight = metrics.getBounds().height();
        } else {
            rawWidth = appContext.getResources().getDisplayMetrics().widthPixels;
            rawHeight = appContext.getResources().getDisplayMetrics().heightPixels;
        }

        if (rawWidth <= 0 || rawHeight <= 0) {
            rawWidth = 720;
            rawHeight = 1280;
        }

        int maxEdge = Math.max(rawWidth, rawHeight);
        float scale = maxEdge > MAX_CAPTURE_EDGE ? (float) MAX_CAPTURE_EDGE / maxEdge : 1f;

        int width = makeEven(Math.max(2, Math.round(rawWidth * scale)));
        int height = makeEven(Math.max(2, Math.round(rawHeight * scale)));
        return new Size(width, height);
    }

    private int makeEven(int value) {
        return value % 2 == 0 ? value : value - 1;
    }
}
