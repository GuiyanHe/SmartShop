package edu.tamu.csce634.smartshop.ui.map;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.Nullable;

import edu.tamu.csce634.smartshop.models.world.Aisle;
import edu.tamu.csce634.smartshop.models.world.SupermarketLayout;

/**
 * A custom View responsible for drawing both the supermarket layout (background)
 * and the animated navigation path (foreground).
 */
public class PathNavigationView extends View {

    // --- Paints ---
    private final Paint pathPaint;
    private final Paint markerPaint;
    private final Paint aislePaint; // New: Paint for drawing aisles

    // --- Path Animation ---
    private final Path fullPath = new Path();
    private final Path animatedPath = new Path();
    private final PathMeasure pathMeasure = new PathMeasure();
    private PointF currentMarkerPosition;
    private float markerRadius = 24f;
    private ValueAnimator pathAnimator;

    // --- World Model Data ---
    private SupermarketLayout supermarketLayout; // New: Holds the layout data

    public PathNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // --- Initialize Paints ---
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#4CAF50")); // Green path
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(18f);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setPathEffect(new DashPathEffect(new float[]{40, 20}, 0));

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(Color.parseColor("#E91E63")); // Pink marker
        markerPaint.setStyle(Paint.Style.FILL);

        // New: Initialize the Paint for aisles
        aislePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        aislePaint.setColor(Color.parseColor("#E0E0E0")); // Light gray for aisles
        aislePaint.setStyle(Paint.Style.FILL);

        setupAnimator();
    }

    private void setupAnimator() {
        pathAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        pathAnimator.setDuration(1200);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            float pathLength = pathMeasure.getLength();
            animatedPath.reset();
            pathMeasure.getSegment(0, pathLength * fraction, animatedPath, true);
            float[] pos = new float[2];
            pathMeasure.getPosTan(pathLength * fraction, pos, null);
            currentMarkerPosition = new PointF(pos[0], pos[1]);
            invalidate();
        });
    }

    /**
     * New: Public method to receive the supermarket layout data.
     * @param layout The parsed layout from supermarket_layout.json.
     */
    public void setSupermarketLayout(SupermarketLayout layout) {
        this.supermarketLayout = layout;
        invalidate(); // Redraw the view to show the new layout
    }

    /**
     * Public method to start drawing a path from a start point to an end point.
     * @param startPoint The starting point in pixels.
     * @param endPoint The ending point in pixels.
     */
    public void drawPath(PointF startPoint, PointF endPoint) {
        if (startPoint == null || endPoint == null) {
            clearPath();
            return;
        }

        fullPath.reset();
        fullPath.moveTo(startPoint.x, startPoint.y);
        fullPath.lineTo(endPoint.x, endPoint.y);
        pathMeasure.setPath(fullPath, false);

        if (pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        pathAnimator.start();
    }

    /**
     * Clears any drawn navigation path from the view.
     */
    public void clearPath() {
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        fullPath.reset();
        animatedPath.reset();
        currentMarkerPosition = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Draw the background layout first
        drawBackgroundLayout(canvas);

        // 2. Draw the animated navigation path on top
        canvas.drawPath(animatedPath, pathPaint);

        // 3. Draw the moving marker on top of everything
        if (currentMarkerPosition != null) {
            canvas.drawCircle(currentMarkerPosition.x, currentMarkerPosition.y, markerRadius, markerPaint);
        }
    }

    /**
     * New: Private helper method to draw the aisles based on the supermarket layout data.
     */
    private void drawBackgroundLayout(Canvas canvas) {
        if (supermarketLayout == null || supermarketLayout.aisles == null) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // If the view has no size yet, we can't draw anything.
        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }

        for (Aisle aisle : supermarketLayout.aisles) {
            // Convert percentage coordinates to pixel coordinates
            float left = aisle.x * viewWidth;
            float top = aisle.y * viewHeight;
            float right = (aisle.x + aisle.width) * viewWidth;
            float bottom = (aisle.y + aisle.height) * viewHeight;

            // Draw the rectangle for the aisle
            canvas.drawRect(left, top, right, bottom, aislePaint);
        }
    }
}
