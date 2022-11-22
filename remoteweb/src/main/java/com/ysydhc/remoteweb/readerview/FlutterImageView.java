package com.ysydhc.remoteweb.readerview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.Locale;

public class FlutterImageView extends View {
    private static final String TAG = "FlutterImageView";

    @NonNull
    private ImageReader imageReader;
    @Nullable
    private Image currentImage;
    @Nullable private Bitmap currentBitmap;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public ImageReader getImageReader() {
        return imageReader;
    }

    public enum SurfaceKind {
        /** Displays the background canvas. */
        background,

        /** Displays the overlay surface canvas. */
        overlay,
    }

    /** The kind of surface. */
    private SurfaceKind kind;

    /** Whether the view is attached to the Flutter render. */
    private boolean isAttachedToFlutterRenderer = true;

    /**
     * Constructs a {@code FlutterImageView} with an {@link ImageReader} that provides
     * the Flutter UI.
     */
    public FlutterImageView(@NonNull Context context) {
        this(context, null);
    }

    public FlutterImageView(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlutterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.imageReader = createImageReader(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.kind = SurfaceKind.background;
        init();
    }

    private void init() {
//        setAlpha(0.0f);
    }

    private static void logW(String format, Object... args) {
        Log.w(TAG, String.format(Locale.US, format, args));
    }

    @TargetApi(19)
    @SuppressLint("WrongConstant") // RGBA_8888 is a valid constant.
    @NonNull
    private static ImageReader createImageReader(int width, int height) {
        if (width <= 0) {
            logW("ImageReader width must be greater than 0, but given width=%d, set width=1", width);
            width = 1;
        }
        if (height <= 0) {
            logW("ImageReader height must be greater than 0, but given height=%d, set height=1", height);
            height = 1;
        }
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            return ImageReader.newInstance(
                    width,
                    height,
                    PixelFormat.RGBA_8888,
                    3,
                    HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE | HardwareBuffer.USAGE_GPU_COLOR_OUTPUT);
        } else {
            return ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3);
        }
    }

    @NonNull
    public Surface getSurface() {
        return imageReader.getSurface();
    }

    /**
     * Invoked by the owner of this {@code FlutterImageView} when it no longer wants to render a
     * Flutter UI to this {@code FlutterImageView}.
     */
    public void detachFromRenderer() {
        if (!isAttachedToFlutterRenderer) {
            return;
        }
        setAlpha(0.0f);
        // Drop the latest image as it shouldn't render this image if this view is
        // attached to the renderer again.
        acquireLatestImage();
        // Clear drawings.
        currentBitmap = null;

        // Close and clear the current image if any.
        closeCurrentImage();
        invalidate();
        isAttachedToFlutterRenderer = false;
    }

    public void pause() {
        // Not supported.
    }

    /**
     * Acquires the next image to be drawn to the {@link Canvas}. Returns true if
     * there's an image available in the queue.
     */
    @TargetApi(19)
    public boolean acquireLatestImage() {
        // 1. `acquireLatestImage()` may return null if no new image is available.
        // 2. There's no guarantee that `onDraw()` is called after `invalidate()`.
        // For example, the device may not produce new frames if it's in sleep mode
        // or some special Android devices so the calls to `invalidate()` queued up
        // until the device produces a new frame.
        // 3. While the engine will also stop producing frames, there is a race condition.
        final Image newImage = imageReader.acquireLatestImage();
        if (newImage != null) {
            // Only close current image after acquiring valid new image
            closeCurrentImage();
            currentImage = newImage;
            invalidate();
        }
        return newImage != null;
    }

    /** Creates a new image reader with the provided size. */
    public void resizeIfNeeded(int width, int height) {
        if (width == imageReader.getWidth() && height == imageReader.getHeight()) {
            return;
        }

        // Close resources.
        closeCurrentImage();
        // Close the current image reader, then create a new one with the new size.
        // Image readers cannot be resized once created.
        closeImageReader();
        imageReader = createImageReader(width, height);
    }

    /**
     * Closes the image reader associated with the current {@code FlutterImageView}.
     *
     * <p>Once the image reader is closed, calling {@code acquireLatestImage} will result in an {@code
     * IllegalStateException}.
     */
    public void closeImageReader() {
        imageReader.close();
    }

    private Runnable drawRunnable = this::invalidate;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentImage != null) {
            updateCurrentBitmap();
        }
        if (currentBitmap != null) {
            canvas.drawBitmap(currentBitmap, 0, 0, null);
        } else {
            canvas.drawColor(Color.BLACK);
        }

//        mHandler.removeCallbacks(drawRunnable);
//        mHandler.postDelayed(drawRunnable, 30);
    }

    private void closeCurrentImage() {
        // Close and clear the current image if any.
        if (currentImage != null) {
            currentImage.close();
            currentImage = null;
        }
    }

    @TargetApi(29)
    private void updateCurrentBitmap() {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            final HardwareBuffer buffer = currentImage.getHardwareBuffer();
            currentBitmap = Bitmap.wrapHardwareBuffer(buffer, ColorSpace.get(ColorSpace.Named.SRGB));
            buffer.close();
        } else {
            final Image.Plane[] imagePlanes = currentImage.getPlanes();
            if (imagePlanes.length != 1) {
                return;
            }

            final Image.Plane imagePlane = imagePlanes[0];
            final int desiredWidth = imagePlane.getRowStride() / imagePlane.getPixelStride();
            final int desiredHeight = currentImage.getHeight();

            if (currentBitmap == null
                    || currentBitmap.getWidth() != desiredWidth
                    || currentBitmap.getHeight() != desiredHeight) {
                currentBitmap =
                        Bitmap.createBitmap(
                                desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
            }
            ByteBuffer buffer = imagePlane.getBuffer();
            buffer.rewind();
            currentBitmap.copyPixelsFromBuffer(buffer);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
//        if (width == imageReader.getWidth() && height == imageReader.getHeight()) {
//            return;
//        }
//        // `SurfaceKind.overlay` isn't resized. Instead, the `FlutterImageView` instance
//        // is destroyed. As a result, an instance with the new size is created by the surface
//        // pool in the native side.
//        if (kind == SurfaceKind.background && isAttachedToFlutterRenderer) {
//            resizeIfNeeded(width, height);
//            // Bind native window to the new surface, and create a new onscreen surface
//            // with the new size in the native side.
//        }
    }
}

