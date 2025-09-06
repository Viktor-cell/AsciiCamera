package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AsciiView extends androidx.appcompat.widget.AppCompatTextView {

    private final Paint paint;
    private CharactersColorsArray chcAscii;

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

        Log.d("ascii_view", "startX: " + startX + " startY: " + startY);


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
        if (chcAscii.getWidth() / 9 >= chcAscii.getHeight() / 16) {
            return (float) getWidth() / chcAscii.getWidth();
        } else {
            return (float) getHeight() / chcAscii.getHeight();
        }
    }

    public Bitmap getAsciiAsBitmap() {
        float textSize = calculateTextSize();

        Bitmap out = Bitmap.createBitmap(
                getWidth(),
                getHeight(),
                Bitmap.Config.ARGB_8888
        );

        draw(new Canvas(out));

        return out;
    }
}
