package com.example.ascii_camera;

import android.os.Parcel;
import android.os.Parcelable;

public class AsciiSettings implements Parcelable {

        public static final String DEFAULT_CHARSET = "`.-':_,^=;><+!rc*/z?sTv)J7(|Fi{C}fI31tluneoZ5Yxjya2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@";
        public static final int DEFAULT_FONT_SIZE = 25;
        public static final boolean DEFAULT_MONOCHROME = false;
        public static final boolean DEFAULT_EDGES = false;
        public static final Creator<AsciiSettings> CREATOR = new Creator<>() {
                @Override
                public AsciiSettings createFromParcel(Parcel in) {
                        return new AsciiSettings(in);
                }

                @Override
                public AsciiSettings[] newArray(int size) {
                        return new AsciiSettings[size];
                }
        };
        private static final int DEFAULT_MIN_MAG = 400;

        private static final boolean DEFAULT_INVERTED = false;

        private static final boolean DEFAULT_JUST_LETTERS = false;

        private String charset;
        private int fontSize; // How many pixels for one letter
        private boolean monochrome;
        private boolean edges;
        private boolean inverted;
        private boolean justLetters;

        public boolean isJustLetters() {
                return justLetters;
        }

        public void setJustLetters(boolean justLetters) {
                this.justLetters = justLetters;
        }

        public boolean isInverted() {
                return inverted;
        }

        public void setInverted(boolean inverted) {
                this.inverted = inverted;
        }

        private int minMag;

        public AsciiSettings(String charset, int fontSize, boolean monochrome, boolean inverted, boolean justLetters,  boolean edges, int minMag) {
                setAll(charset, fontSize, monochrome, inverted, justLetters, edges, minMag);
        }

        protected AsciiSettings(Parcel in) {
                charset = in.readString();
                fontSize = in.readInt();
                monochrome = in.readByte() != 0;
                inverted = in.readByte() != 0;
                justLetters = in.readByte() != 0;
                edges = in.readByte() != 0;
                minMag = in.readInt();
        }

        public static AsciiSettings defaultValues() {
                return new AsciiSettings(DEFAULT_CHARSET, DEFAULT_FONT_SIZE, DEFAULT_MONOCHROME, DEFAULT_INVERTED, DEFAULT_JUST_LETTERS, DEFAULT_EDGES, DEFAULT_MIN_MAG);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(charset);
                dest.writeInt(fontSize);
                dest.writeByte((byte) (monochrome ? 1 : 0));
                dest.writeByte((byte) (inverted ? 1 : 0));
                dest.writeByte((byte) (justLetters ? 1 : 0));
                dest.writeByte((byte) (edges ? 1 : 0));
                dest.writeInt(minMag);
        }

        @Override
        public int describeContents() {
                return 0;
        }

        public String getCharset() {
                return charset;
        }

        public void setCharset(String charset) {
                this.charset = charset;
        }

        public int getFontSize() {
                return fontSize;
        }

        public void setFontSize(int fontSize) {
                this.fontSize = fontSize;
        }

        public boolean isMonochrome() {
                return monochrome;
        }

        public void setMonochrome(boolean monochrome) {
                this.monochrome = monochrome;
        }

        public boolean isEdges() {
                return edges;
        }

        public void setEdges(boolean edges) {
                this.edges = edges;
        }

        public int getMinMag() {
                return minMag;
        }

        public void setMinMag(int minMag) {
                this.minMag = minMag;
        }

        public void setAll(String charset, int fontSize, boolean monochrome, boolean inverted, boolean justLetters, boolean edges, int minMag) {
                setCharset(charset);
                setFontSize(fontSize);
                setMonochrome(monochrome);
                setInverted(inverted);
                setJustLetters(justLetters);
                setEdges(edges);
                setMinMag(minMag);
        }

        public void set(AsciiSettings other) {
                setAll(other.getCharset(), other.getFontSize(), other.isMonochrome(), other.isInverted(), other.isJustLetters(), other.isEdges(), other.getMinMag());
        }

        @Override
        public String toString() {
                return "AsciiSettings{" +
                        "charset='" + charset + '\'' +
                        ", fontSize=" + fontSize +
                        ", monochrome=" + monochrome +
                        ", edges=" + edges +
                        ", minMag=" + minMag +
                        '}';
        }
}
