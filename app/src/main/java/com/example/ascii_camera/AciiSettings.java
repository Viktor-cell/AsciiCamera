package com.example.ascii_camera;

import android.os.Parcel;
import android.os.Parcelable;

public class AciiSettings implements Parcelable {
    private String charset;
    private int fontSize; // How many pixels is in one letter
    private boolean monochrome;
    private boolean edges;
    private int minMag;
    public static final String DEFAULT_CHARSET = ".;oO@#";
    public static final int DEFAULT_FONT_SIZE = 8;
    public static final boolean DEFAULT_MONOCHROME = false;
    public static final boolean DEFAULT_EDGES = false;
    private static final int DEFAULT_MIN_MAG = 400;

    protected AciiSettings(Parcel in) {
        charset = in.readString();
        fontSize = in.readInt();
        monochrome = in.readByte() != 0;
        edges = in.readByte() != 0;
        minMag = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(charset);
        dest.writeInt(fontSize);
        dest.writeByte((byte) (monochrome ? 1 : 0));
        dest.writeByte((byte) (edges ? 1 : 0));
        dest.writeInt(minMag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AciiSettings> CREATOR = new Creator<AciiSettings>() {
        @Override
        public AciiSettings createFromParcel(Parcel in) {
            return new AciiSettings(in);
        }

        @Override
        public AciiSettings[] newArray(int size) {
            return new AciiSettings[size];
        }
    };

    @Override
    public String toString() {
        return "AciiSettings{" +
                "charset='" + charset + '\'' +
                ", fontSize=" + fontSize +
                ", monochrome=" + monochrome +
                ", edges=" + edges +
                '}';
    }


    public void setAll(String charset, int fontSize, boolean monochrome, boolean edges, int minMag) {
        setCharset(charset);
        setEdges(edges);
        setMonochrome(monochrome);
        setFontSize(fontSize);
        setMinMag(minMag);
    }

    public AciiSettings(String charset, int fontSize, boolean monochrome, boolean edges, int minMag) {
        setAll(charset, fontSize, monochrome, edges, minMag);
    }

    public AciiSettings(AciiSettings other) {
        setAll(other.getCharset(), other.getFontSize(), other.isMonochrome(), other.isEdges(), other.getMinMag());
    }

    public static AciiSettings defaultValues() {
        return new AciiSettings(DEFAULT_CHARSET, DEFAULT_FONT_SIZE, DEFAULT_MONOCHROME, DEFAULT_EDGES, DEFAULT_MIN_MAG);
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public boolean isEdges() {
        return edges;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getCharset() {
        return charset;
    }

    public int getMinMag() {
        return minMag;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setEdges(boolean edges) {
        this.edges = edges;
    }

    public void setMinMag(int minMag) {
        this.minMag = minMag;
    }

    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }
}
