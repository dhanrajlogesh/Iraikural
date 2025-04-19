package com.example.iraikural;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WaveformView extends View {

    private Paint paint;
    private List<Float> heights = new ArrayList<>();
    private int barWidth = 20;
    private int barSpacing = 8;
    private int sidePadding = 20;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setWaveform(List<Float> heights) {
        this.heights = (heights != null) ? heights : new ArrayList<>();
        Log.d("WaveformView", "setWaveform called with " + this.heights.size() + " heights");
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth = (int) (300 * getResources().getDisplayMetrics().density);
        int defaultHeight = (int) (150 * getResources().getDisplayMetrics().density);
        int width = resolveSize(defaultWidth, widthMeasureSpec);
        int height = resolveSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (heights.isEmpty()) return;

        float totalWidth = heights.size() * (barWidth + barSpacing) - barSpacing;
        float startX = (getWidth() - totalWidth) / 2 + sidePadding;
        float centerY = getHeight() / 2f;

        for (int i = 0; i < heights.size(); i++) {
            float barHeight = heights.get(i);
            float left = startX + i * (barWidth + barSpacing);
            float right = left + barWidth;
            float top = centerY - barHeight / 2;
            float bottom = centerY + barHeight / 2;
            float radius = barWidth / 2f;

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }
    }
}