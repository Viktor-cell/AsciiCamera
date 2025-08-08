package com.example.ascii_camera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Pair;

import java.io.Serializable;
import java.util.HashMap;

public class Ascii implements Serializable {
    private Bitmap bmp;
    private String charset;
    private int fontSize; // How many pixels is in one letter
    private boolean monochrome;
    private boolean edges;
    private HashMap<Pair<Integer, Integer>, Character> edgePositions;
    private SpannableString ssAsciiText;

    public Ascii(Bitmap bmp) {
        charset = " .:ocOC@#";
        fontSize = 8;
        monochrome = false;
        edges = false;
        this.bmp = Bitmap.createScaledBitmap(bmp,
                bmp.getWidth() / fontSize,
                bmp.getHeight() / fontSize,
                true);
        Log.d("Ascii", this.toString());
    }
    @Override
    public String toString() {
        return "Ascii{" +
                "bmp=" + bmp +
                ", charset='" + charset + '\'' +
                ", fontSize=" + fontSize +
                ", monochrome=" + monochrome +
                ", edges=" + edges +
                '}';
    }

    private void generateColoredText() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (int x = 0; x < bmp.getWidth() / fontSize; x++ ) {
            for (int y = 0; y < bmp.getHeight() / fontSize; y++ ) {
                    // TODO finish
            }
        }
    }

    private void findEdges() {
        int[][] kernelGx = new int[][]{
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        int[][] kernelGy = new int[][]{
                {1, 2, 1},
                {0, 0, 0},
                {-1, -2, -1}
        };

        double magY, magX;

        for (int x = 0; x < bmp.getWidth(); x++) {
            for (int y = 0; x < bmp.getHeight(); y++) {
                magX = magY = 0;

                for (int kx = 0; kx < 3; kx++) {
                    for (int ky = 0; ky < 3; ky++) {
                        var pixel = bmp.getPixel(x, y);

                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);

                        int valGx = kernelGx[kx][ky];
                        int valGy = kernelGy[kx][ky];

                        magX += r * valGx;
                        magY += r * valGy;

                        magX += g * valGx;
                        magY += g * valGy;

                        magX += b * valGx;
                        magY += b * valGy;

                        // TODO: 8. 8. 2025 finish after adding minimum magnitude level
                    }
                }

            }
        }
    }

    private int calculateLightness(int pixel) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }
}
