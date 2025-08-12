package com.example.ascii_camera;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class CharactersColorsArray implements Parcelable {
    public static final Creator<CharactersColorsArray> CREATOR = new Creator<CharactersColorsArray>() {
        @Override
        public CharactersColorsArray createFromParcel(Parcel in) {
            return new CharactersColorsArray(in);
        }

        @Override
        public CharactersColorsArray[] newArray(int size) {
            return new CharactersColorsArray[size];
        }
    };
    private char[] characters;
    private int[] colors; // argb format
    private int width;
    private int height;

    public CharactersColorsArray(int width, int height) {
        this.width = width;
        this.height = height;
        characters = new char[width * height];
        colors = new int[width * height];

    }

    protected CharactersColorsArray(Parcel in) {
        characters = in.createCharArray();
        colors = in.createIntArray();
        width = in.readInt();
        height = in.readInt();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    void setCharacter(int x, int y, char c) {
        characters[y * width + x] = c;
    }

    void setColor(int x, int y, int c) {
        colors[y * width + x] = c;
    }

    char getCharacter(int x, int y) {
        return characters[y * width + x];
    }

    @Override
    public String toString() {
        return "CharactersColorsArray{" +
                "characters=" + Arrays.toString(characters) +
                ", colors=" + Arrays.toString(colors) +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    int getColor(int x, int y) {
        return colors[y * width + x];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharArray(characters);
        dest.writeIntArray(colors);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
