package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;



import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class Ascii implements Parcelable {
    private Bitmap bmp;
    private AciiSettings settings;

    // TODO: 11. 8. 2025 change theese two for arrays of characters and colors 
    private HashMap<Pair<Integer, Integer>, Character> edgePositions;
    private SpannableStringBuilder ssbAsciiText;


    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public void setSsAsciiText(SpannableStringBuilder ssbAsciiText) {
        this.ssbAsciiText = ssbAsciiText;
    }

    public void setEdgePositions(HashMap<Pair<Integer, Integer>, Character> edgePositions) {
        this.edgePositions = edgePositions;
    }

    public void setSettings(AciiSettings settings) {
        this.settings = settings;
    }

    public SpannableStringBuilder getSsbAsciiText() {
        return ssbAsciiText;
    }

    public HashMap<Pair<Integer, Integer>, Character> getEdgePositions() {
        return edgePositions;
    }

    public AciiSettings getSettings() {
        return settings;
    }

    public Bitmap getBmp() {
        return bmp;
    }


    // NOTE: Only init bmp and settings here, edge positions and ascii text are not Parceable so after sending it they will be null
    public Ascii(Context context, Uri uri, AciiSettings settings) {
        this.settings = new AciiSettings(settings);

        InputStream inputStream = null;

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


    }

    // Generated parceable interface by android studio
    // NOTE: If you would need to send edge data or the ascii string it will not work!!!

    protected Ascii(Parcel in) {
        bmp = in.readParcelable(Bitmap.class.getClassLoader());
        settings = in.readParcelable(AciiSettings.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bmp, flags);
        dest.writeParcelable(settings, flags);
    }

    @Override
    public int describeContents() {
        return 0;
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
    public String toString() {
        return "Ascii{" +
                "bmp=" + bmp +
                ", settings=" + settings +
                ", edgePositions=" + edgePositions +
                ", ssbAsciiText=" + ssbAsciiText +
                '}';
    }

    // TODO: 11. 8. 2025 change 
    public void generateColoredText() {
        ssbAsciiText = new SpannableStringBuilder();
        for (int y = 0; y < bmp.getHeight(); y++ ) {
            for (int x = 0; x < bmp.getWidth(); x++ ) {
                int charIndex =  (int)(calculateLightness(bmp.getPixel(x, y)) / 255f * settings.getCharset().length());
                ssbAsciiText.append(settings.getCharset().charAt(charIndex));

                ssbAsciiText.setSpan(new ForegroundColorSpan(bmp.getPixel(x, y)),
                        ssbAsciiText.length() - 1,
                        ssbAsciiText.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE );
            }
            ssbAsciiText.append("\n");
        }
    }

    // TODO: 11. 8. 2025 change
    private void applySobel() {
        edgePositions = new HashMap<>();
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
                            edgePositions.put(new Pair<>(x, y), getEdgeCharacter(magX, magY));
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
}
