package com.example.ascii_camera;

import static android.view.View.MeasureSpec.EXACTLY;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FullAscii {
        private String author, artName;
        private CharactersColorsArray chcArray;

        public FullAscii(String author, String artName, int width, int height, int[] colors, char[] characters) {
                this.artName = artName;
                this.author = author;
                chcArray = new CharactersColorsArray(width, height, colors, characters);
        }

        public static FullAscii fromJSONObject(JSONObject json) {
                try {
                        String author = json.getString("author");
                        String artName = json.getString("artName");
                        int width = json.getInt("width");
                        int height = json.getInt("height");
                        int[] colors = Utils.JSONArrayToIntArray(json.getJSONArray("colors"));
                        char[] characters = Utils.JSONArrayToCharArray(json.getJSONArray("letters"));

                        return new FullAscii(author, artName, width, height, colors, characters);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public static ArrayList<FullAscii> fromJSONArray(JSONArray jsonArray) {
                try {
                        int size = jsonArray.length();
                        ArrayList<FullAscii> fullAsciis = new ArrayList<>();

                        for (int i = 0; i < size; i++) {
                                fullAsciis.add(fromJSONObject(jsonArray.getJSONObject(i)));
                        }

                        return fullAsciis;

                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public AsciiView toAsciiView(Context ctx) {
                AsciiView asciiView = new AsciiView(ctx, null);

                int viewWidth = chcArray.getWidth() * 25;
                int viewHeigth = chcArray.getHeight() * 25;
                asciiView.setChcAscii(chcArray);

                asciiView.measure(
                        View.MeasureSpec.makeMeasureSpec(viewWidth, EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(viewHeigth, EXACTLY)
                );
                asciiView.layout(0, 0, viewWidth, viewHeigth);

                return asciiView;
        }

        public Bitmap toAsciiBitmap(Context ctx) {
                return toAsciiView(ctx).getAsciiAsBitmap(true);
        }

        @Override
        public String toString() {
                return "FullAscii{" +
                        "author='" + author + '\'' +
                        ", artName='" + artName + '\'' +
                        ", chcArray=" + chcArray +
                        '}';
        }

        public CharactersColorsArray getChcArray() {
                return chcArray;
        }

        public void setChcArray(CharactersColorsArray chcArray) {
                this.chcArray = chcArray;
        }

        public String getArtName() {
                return artName;
        }

        public void setArtName(String artName) {
                this.artName = artName;
        }

        public String getAuthor() {
                return author;
        }

        public void setAuthor(String author) {
                this.author = author;
        }
}
