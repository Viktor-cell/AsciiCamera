package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Ascii implements Parcelable {
    private Bitmap bmp;

    private AciiSettings settings;

    private CharactersColorsArray chcArray;

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public void setSettings(AciiSettings settings) {
        this.settings = settings;
    }

    public AciiSettings getSettings() {
        return settings;
    }

    public Bitmap getBmp() {
        return bmp;
    }


    public Ascii(Context context, Uri uri, AciiSettings settings) {

        InputStream inputStream;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Bitmap tmp = BitmapFactory.decodeStream(inputStream);

        if (tmp == null) {
            throw new RuntimeException("Selected file not found");
        }

        this.bmp = Bitmap.createScaledBitmap(tmp,
                tmp.getWidth() / settings.getFontSize(),
                tmp.getHeight() / settings.getFontSize(),
                true);

        this.settings = new AciiSettings(settings);
        this.chcArray = new CharactersColorsArray(bmp.getWidth(), bmp.getHeight());
    }

    public void generateColoredText() {
        // TODO: 11. 8. 2025 implement
        int charIndex;
        for (int y = 0; y < chcArray.getHeight(); y++) {
            for (int x = 0; x < chcArray.getWidth(); x++) {
                charIndex = (int)(calculateLightness(chcArray.getColor(x, y)) / 255f * settings.getCharset().length());
                chcArray.setCharacter(x, y, settings.getCharset().charAt(charIndex));
                chcArray.setColor(x, y, bmp.getPixel(x, y));
            }
        }
    }

    private void applySobel() {
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

                        if ( Math.pow(magX, 2) + Math.pow(magY, 2) > settings.getMinMag() ) {
                            chcArray.setCharacter(x, y, getEdgeCharacter(magX, magY));
                        }
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

    private char getEdgeCharacter(double magX, double magY) {
        double angle = Math.atan2(magY, magX) / Math.PI * 180;

        if ((angle > -22.5 && angle <= 22.5) || (angle < -157.5 || angle >= 157.5)) {
            return '|';
        } else if ((angle > 22.5 && angle <= 67.5) || (angle < -112.5 && angle >= -157.5)) {
            return '\\';
        } else if ((angle > 67.5 && angle <= 112.5) || (angle < -67.5 && angle >= -112.5)) {
            return '-';
        } else if ((angle > 112.5 && angle <= 157.5) || (angle < -22.5 && angle >= -67.5)) {
            return '/';
        } else {
            return ' ';
        }

    }
    protected Ascii(Parcel in) {
        bmp = in.readParcelable(Bitmap.class.getClassLoader());
        settings = in.readParcelable(AciiSettings.class.getClassLoader());
        chcArray = in.readParcelable(CharactersColorsArray.class.getClassLoader());
    }

    public static final Creator<Ascii> CREATOR = new Creator<Ascii>() {
        @Override
        public Ascii createFromParcel(Parcel in) {
            return new Ascii(in);
        }

        @Override
        public Ascii[] newArray(int size) {
            return new Ascii[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(bmp, flags);
        dest.writeParcelable(settings, flags);
        dest.writeParcelable(chcArray, flags);
    }
}
