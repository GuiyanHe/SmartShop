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

public class PathNavigationView extends View {

    private final Paint pathPaint;
    private final Paint markerPaint;
    private final Paint aislePaint;

    private final Path fullPath = new Path();
    private final Path animatedPath = new Path();
    private final PathMeasure pathMeasure = new PathMeasure();

    private PointF currentMarkerPosition;
    private float markerRadius = 24f;
    private ValueAnimator pathAnimator;

    private SupermarketLayout supermarketLayout;

    public PathNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#4CAF50"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(18f);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setPathEffect(new DashPathEffect(new float[]{40, 20}, 0));

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(Color.parseColor("#E91E63"));
        markerPaint.setStyle(Paint.Style.FILL);

        aislePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        aislePaint.setColor(Color.parseColor("#E0E0E0"));
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

    public void setSupermarketLayout(SupermarketLayout layout) {
        this.supermarketLayout = layout;
        invalidate();
    }

    // THIS IS THE CORE FIX: The startPoint and endPoint are already calculated pixel values from MapFragment.
    // We don't need to do any more calculations here. Just draw.
    public void drawPath(PointF startPointInPixels, PointF endPointInPixels) {
        if (startPointInPixels == null || endPointInPixels == null) {
            clearPath();
            return;
        }

        fullPath.reset();
        fullPath.moveTo(startPointInPixels.x, startPointInPixels.y);
        fullPath.lineTo(endPointInPixels.x, endPointInPixels.y);
        pathMeasure.setPath(fullPath, false);

        if (pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        pathAnimator.start();
    }

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
        drawBackgroundLayout(canvas);
        canvas.drawPath(animatedPath, pathPaint);
        if (currentMarkerPosition != null) {
            canvas.drawCircle(currentMarkerPosition.x, currentMarkerPosition.y, markerRadius, markerPaint);
        }
    }

    // This logic is also simplified. It draws based on the whole View's size.
    private void drawBackgroundLayout(Canvas canvas) {
        if (supermarketLayout == null || supermarketLayout.aisles == null) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }
        for (Aisle aisle : supermarketLayout.aisles) {
            float left = aisle.x * viewWidth;
            float top = aisle.y * viewHeight;
            float right = (aisle.x + aisle.width) * viewWidth;
            float bottom = (aisle.y + aisle.height) * viewHeight;
            canvas.drawRect(left, top, right, bottom, aislePaint);
        }
    }
}
