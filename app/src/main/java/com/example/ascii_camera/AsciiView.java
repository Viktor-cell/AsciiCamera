package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AsciiView extends androidx.appcompat.widget.AppCompatTextView {

    private CharactersColorsArray chcAscii;
    private final Paint paint;

    public AsciiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setChcAscii(CharactersColorsArray chcAscii) {
        this.chcAscii = chcAscii;
    }

    public void redraw() {
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (chcAscii == null) {
            Log.w("ascii_view", "CharactersColorsArray is null, skipping drawing");
            return;
        }

        Log.d("ascii_view", chcAscii.toString());

        float textSize = calculateTextSize();
        paint.setTextSize(textSize);

        float startY = (getHeight() - chcAscii.getHeight() * textSize) / 2 + textSize;
        float startX = (getWidth() - chcAscii.getWidth() * textSize) / 2;
        float step = textSize;

        for (int y = 0; y < chcAscii.getHeight(); y++) {
            for (int x = 0; x < chcAscii.getWidth(); x++) {
                paint.setColor(chcAscii.getColor(x, y));

                float canvasPosX = startX + x * step;
                float canvasPosY = startY + y * step;

                canvas.drawText(
                        String.valueOf(chcAscii.getCharacter(x, y)),
                        canvasPosX,
                        canvasPosY,
                        paint
                );
            }
        }
    }

    private float calculateTextSize() {
        if (chcAscii == null || chcAscii.getWidth() == 0 || chcAscii.getHeight() == 0) {
            return 12f; // fallback text size
        }
        if (chcAscii.getWidth() > chcAscii.getHeight()) {
            return (float) getWidth() / chcAscii.getWidth();
        } else {
            return (float) getHeight() / chcAscii.getHeight();
        }
    }
}
