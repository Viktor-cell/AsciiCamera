package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.InputStream;

public class Ascii implements Parcelable {
        public static final Creator<Ascii> CREATOR = new Creator<>() {
                @Override
                public Ascii createFromParcel(Parcel in) {
                        return new Ascii(in);
                }

                @Override
                public Ascii[] newArray(int size) {
                        return new Ascii[size];
                }
        };
        private final Uri uriBmp;
        private Bitmap bmpScaled;
        private Bitmap bmpOriginal;
        private CharactersColorsArray chcArray;
        private AsciiSettings settings;

        public Ascii(Parcel in) {
                settings = in.readParcelable(AsciiSettings.class.getClassLoader());
                uriBmp = in.readParcelable(Uri.class.getClassLoader());
        }

        public Ascii(Uri uriBmp, AsciiSettings settings) {
                this.uriBmp = uriBmp;
                this.settings = settings;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
                dest.writeParcelable(settings, flags);
                dest.writeParcelable(uriBmp, flags);
        }

        @Override
        public int describeContents() {
                return 0;
        }

        public Bitmap getBmpOriginal() {
                return bmpOriginal;
        }

        public void setBmpOriginal(Bitmap bmpOriginal) {
                this.bmpOriginal = bmpOriginal;
        }

        public Bitmap getBmpScaled() {
                return bmpScaled;
        }

        public void setBmpScaled(Bitmap bmpScaled) {
                this.bmpScaled = bmpScaled;
        }

        public CharactersColorsArray getChcArray() {
                return chcArray;
        }

        public void setChcArray(CharactersColorsArray chcArray) {
                this.chcArray = chcArray;
        }

        public AsciiSettings getSettings() {
                return settings;
        }

        public void setSettings(AsciiSettings settings) {
                this.settings = settings;
                createBmpScaled();
                this.chcArray = new CharactersColorsArray(bmpScaled.getWidth(), bmpScaled.getHeight());
        }

        public void initBmpIfNeeded(Context context) {
                if (bmpOriginal != null) {
                        return;
                }

                try (InputStream inputStream = context.getContentResolver().openInputStream(uriBmp)) {
                        bmpOriginal = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                createBmpScaled();
                this.chcArray = new CharactersColorsArray(bmpScaled.getWidth(), bmpScaled.getHeight());
        }

        public void createBmpScaled() {
                this.bmpScaled = Bitmap.createScaledBitmap(
                        bmpOriginal,
                        bmpOriginal.getWidth() / settings.getFontSize(),
                        bmpOriginal.getHeight() / settings.getFontSize(),
                        true);
        }

        public void generateColoredText() {
                int charIndex;
                for (int y = 0; y < chcArray.getHeight(); y++) {
                        for (int x = 0; x < chcArray.getWidth(); x++) {

                                // create array
                                charIndex = (int) (calculateLightness(bmpScaled.getPixel(x, y)) / 255f * settings.getCharset().length());
                                chcArray.setCharacter(x, y, settings.getCharset().charAt(charIndex));

                                if (settings.isMonochrome()) {
                                        int shade = calculateLightness(bmpScaled.getPixel(x, y));
                                        int color = Color.rgb(shade, shade, shade);
                                        chcArray.setColor(x, y, color);
                                } else {
                                        int color = bmpScaled.getPixel(x, y);
                                        chcArray.setColor(x, y, color);
                                }
                                // scaling gets done in createBmpScaled
                                // min mag in apply sobel
                                // charset in this function
                        }
                }

                if (settings.isEdges()) {
                        applySobel();
                }
        }

        private void applySobel() {
                int[][] kernelGx = {
                        {-1, 0, 1},
                        {-2, 0, 2},
                        {-1, 0, 1}
                };
                int[][] kernelGy = {
                        {1, 2, 1},
                        {0, 0, 0},
                        {-1, -2, -1}
                };

                float minMag = settings.getMinMag();
                float minMagSquared = minMag * minMag;

                int height = bmpScaled.getHeight();
                int width = bmpScaled.getWidth();
                int[] pixels = new int[width * height];
                bmpScaled.getPixels(pixels, 0, width, 0, 0, width, height);

                for (int y = 1; y < height - 1; y++) {
                        for (int x = 1; x < width - 1; x++) {
                                double magX = 0;
                                double magY = 0;

                                for (int ky = -1; ky <= 1; ky++) {
                                        for (int kx = -1; kx <= 1; kx++) {
                                                int pixelColor = pixels[(y + ky) * width + (x + kx)];
                                                int lightness = calculateLightness(pixelColor);

                                                int weightGx = kernelGx[ky + 1][kx + 1];
                                                int weightGy = kernelGy[ky + 1][kx + 1];

                                                magX += lightness * weightGx;
                                                magY += lightness * weightGy;
                                        }
                                }

                                double magnitudeSquared = magX * magX + magY * magY;

                                if (magnitudeSquared > minMagSquared) {
                                        char edgeChar = getEdgeCharacter(magX, magY);
                                        chcArray.setCharacter(x, y, edgeChar);
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

        private char getEdgeCharacter(double magX, double magY) {
                double angle = Math.toDegrees(Math.atan2(magY, magX));

                if ((angle > -22.5 && angle <= 22.5) || angle <= -157.5 || angle > 157.5) {
                        return '|';
                } else if ((angle > 22.5 && angle <= 67.5) || (angle <= -112.5 && angle > -157.5)) {
                        return '\\';
                } else if ((angle > 67.5 && angle <= 112.5) || (angle <= -67.5 && angle > -112.5)) {
                        return '-';
                } else if ((angle > 112.5 && angle <= 157.5) || (angle <= -22.5 && angle > -67.5)) {
                        return '/';
                } else {
                        return ' ';
                }
        }

        @Override
        public String toString() {
                return "Ascii{" +
                        "bmpScaled=" + bmpScaled +
                        ", bmpOriginal=" + bmpOriginal +
                        ", settings=" + settings +
                        ", chcArray=" + chcArray +
                        '}';
        }
}