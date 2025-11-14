package edu.tamu.csce634.smartshop.ui.home;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import edu.tamu.csce634.smartshop.R;

public class CircularProgressView extends View {

    private int progress;
    private float progressWidth = 8f; // default
    private int progressColor = Color.GREEN;
    private int backgroundColor = Color.LTGRAY; // Default background color
    private String progressText = "0";
    private float progressTextSize = 40f; // default
    private int progressTextColor = Color.BLACK;

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private RectF oval;
    private Rect textBounds;

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // Default background color from our new colors.xml
        int defaultBgColor = ContextCompat.getColor(context, R.color.stat_background_gray);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularProgressView, 0, 0);
        try {
            progress = a.getInteger(R.styleable.CircularProgressView_progress, 0);
            progressWidth = a.getDimension(R.styleable.CircularProgressView_progressWidth, 8f);
            progressColor = a.getColor(R.styleable.CircularProgressView_progressColor, Color.GREEN);
            backgroundColor = a.getColor(R.styleable.CircularProgressView_backgroundColor, defaultBgColor); // Updated default
            progressText = a.getString(R.styleable.CircularProgressView_progressText);
            if (progressText == null) {
                progressText = "0";
            }
            progressTextSize = a.getDimension(R.styleable.CircularProgressView_progressTextSize, 40f);
            progressTextColor = a.getColor(R.styleable.CircularProgressView_progressTextColor, Color.BLACK);

        } finally {
            a.recycle();
        }

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND); // Makes the arc ends round

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(progressWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(progressTextColor);
        textPaint.setTextSize(progressTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        oval = new RectF();
        textBounds = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - (progressWidth / 2f); // Radius

        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw the background circle
        canvas.drawArc(oval, 0, 360, false, backgroundPaint);

        // Draw the progress arc
        float sweepAngle = 360f * progress / 100;
        canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);

        // Draw the text
        textPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);
        float textHeight = textBounds.height();
        canvas.drawText(progressText, centerX, centerY + (textHeight / 2f), textPaint);
    }

    // Public setters to update the view
    public void setProgress(int progress) {
        this.progress = Math.min(Math.max(progress, 0), 100); // Clamp between 0 and 100
        invalidate(); // Redraw the view
    }

    public void setProgressText(String text) {
        this.progressText = text;
        invalidate(); // Redraw the view
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setProgressTextColor(int color) {
        this.progressTextColor = color;
        textPaint.setColor(color);
        invalidate();
    }
}