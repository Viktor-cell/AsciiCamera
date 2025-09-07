package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileNotFoundException;
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

    // Getters and setters
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Bitmap Uri not found", e);
        } catch (Exception e) {
            throw new RuntimeException("Error decoding bitmap", e);
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

        double magX, magY;
        float minMag = settings.getMinMag();
        int height = bmpScaled.getHeight();
        int width = bmpScaled.getWidth();
        int[] pixels = new int[width * height];
        bmpScaled.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int y = 1; y < height - 2; y++) {
            for (int x = 1; x < width - 2; x++) {
                magX = 0;
                magY = 0;

                for (int kx = 0; kx < 3; kx++) {
                    for (int ky = 0; ky < 3; ky++) {
                        int currentPixel = pixels[(y + ky) * width + x + kx];

                        int r = (currentPixel >> 16) & 0xff;
                        int g = (currentPixel >> 8) & 0xff;
                        int b = currentPixel & 0xff;

                        int valGx = kernelGx[ky][kx];
                        int valGy = kernelGy[ky][kx];

                        magX += r * valGx;
                        magY += r * valGy;

                        magX += g * valGx;
                        magY += g * valGy;

                        magX += b * valGx;
                        magY += b * valGy;
                    }
                }

                if (magX * magX + magY * magY > minMag * minMag) {
                    chcArray.setCharacter(x, y, getEdgeCharacter(magX, magY));
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
