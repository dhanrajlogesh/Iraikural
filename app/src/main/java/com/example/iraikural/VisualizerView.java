package com.example.iraikural;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class VisualizerView extends View {
    private byte[] waveformData;
    private final Paint paint = new Paint();
    private final Random random = new Random();

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.YELLOW);
        paint.setStrokeCap(Paint.Cap.ROUND); // Rounded bar edges
        paint.setStyle(Paint.Style.FILL);
    }

    public void updateVisualizer(byte[] waveform) {
        this.waveformData = waveform.clone();
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (waveformData == null || waveformData.length == 0) return;

        float width = getWidth();
        float height = getHeight();
        int barCount = 20; // Number of bars (adjust for spacing)
        float barWidth = width / (barCount * 1.5f); // Spaced bars

        for (int i = 0; i < barCount; i++) {
            float x = i * barWidth * 1.5f + barWidth / 2; // Positioning
            float barHeight = (waveformData[i % waveformData.length] + 128) * height / 256;
            barHeight = Math.max(barHeight, height * 0.2f); // Min height
            barHeight *= 0.8f + random.nextFloat() * 0.4f; // Random variation

            canvas.drawRoundRect(
                    x, height - barHeight, x + barWidth, height, // Bar position
                    barWidth / 2, barWidth / 2, // Rounded corners
                    paint
            );
        }
    }
}
