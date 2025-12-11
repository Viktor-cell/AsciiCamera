package com.example.ascii_camera;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Utils {

        public static final String LOGGED_OUT_USERNAME = "anonymous";
        private static final String PREFS_LOCATION = "user_data";

        public static void sendAsciiToOnlineGallery(FullAscii fullAscii, Context ctx, Activity act) {

                JSONObject json = new JSONObject();

                JSONArray letters = new JSONArray();
                for (char letter : fullAscii.getChcArray().getCharacters()) {
                        letters.put(String.valueOf(letter));
                }

                JSONArray colors = new JSONArray();
                for (int color : fullAscii.getChcArray().getColors()) {
                        colors.put(color);
                }


                try {
                        json.put("author", fullAscii.getAuthor());
                        json.put("artName", fullAscii.getArtName());
                        json.put("width", fullAscii.getChcArray().getWidth());
                        json.put("height", fullAscii.getChcArray().getHeight());
                        json.put("letters", letters);
                        json.put("colors", colors);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                ServerUtils.post(json.toString(), "art/upload", new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                act.runOnUiThread(() -> {
                                        Toast.makeText(ctx, "Something went wrong", Toast.LENGTH_SHORT).show();
                                });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                if (response.isSuccessful()) {
                                        act.runOnUiThread(() -> {
                                                Toast.makeText(ctx, "Image send successfully", Toast.LENGTH_SHORT).show();
                                        });
                                        Intent intent = new Intent(ctx, MainActivityLocalGallery.class);
                                        act.startActivity(intent);
                                }
                        }
                });
        }

        public static void saveLocaly(String fileName, Bitmap bmp, Context ctx) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "ascii_" + fileName + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                ContentResolver resolver = ctx.getContentResolver();

                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try {
                        OutputStream outStream = resolver.openOutputStream(uri);
                        bmp.compress(Bitmap.CompressFormat.PNG, 75, outStream);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

        }

        public static void createTmpFolder(Context ctx, String name) {
                File dir = ctx.getCacheDir();
                File tmpImage = new File(dir + "/" + name);
                if (!tmpImage.exists()) tmpImage.mkdir();
        }

        public static void cleanTmpFolder(Context ctx, String name) {
                File dir = new File(ctx.getCacheDir() + "/" + name);
                if (!dir.exists()) {
                        return;
                } else {
                        dir.listFiles();
                }
                for (File file : dir.listFiles()) {
                        file.delete();
                }
        }

        public static void getPermissions(Context ctx, Activity act) {
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(act,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
        }

        public static void addStringToPrefs(String key, String val, Context ctx) {
                SharedPreferences sp = ctx.getSharedPreferences(PREFS_LOCATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.putString(key, val);
                editor.apply();
        }

        public static void editStringInPrefs(String key, String val, Context ctx) {
                SharedPreferences sp = ctx.getSharedPreferences(PREFS_LOCATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.remove(key);
                editor.putString(key, val);
                editor.apply();
        }

        public static void removeStringToPrefs(String key, Context ctx) {
                SharedPreferences sp = ctx.getSharedPreferences(PREFS_LOCATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.remove(key);
                editor.apply();
        }

        public static String getStringFromPrefs(String key, Context ctx) {
                SharedPreferences sp = ctx.getSharedPreferences(PREFS_LOCATION, MODE_PRIVATE);
                return sp.getString(key, "");
        }

        public static String hash(String s) {
                String hash;
                try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-512");
                        byte[] hashBytes = digest.digest(s.getBytes());
                        hash = Base64.encodeToString(hashBytes, Base64.DEFAULT);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                return hash;
        }

        public static int[] JSONArrayToIntArray(JSONArray array) throws JSONException {
                int[] res = new int[array.length()];
                for (int i = 0; i < array.length(); i++) {
                        res[i] = array.getInt(i);
                }
                return res;
        }

        public static char[] JSONArrayToCharArray(JSONArray array) throws JSONException {
                char[] res = new char[array.length()];
                for (int i = 0; i < array.length(); i++) {
                        res[i] = array.getString(i).charAt(0);
                }
                return res;
        }

        public static View createLocalGallery(ArrayList<Uri> images, Context ctx) {
                if (images.isEmpty()) {
                        TextView tv = new TextView(ctx);
                        tv.setText("No image found");
                        return tv;
                }
                RecyclerView rvGallery = new RecyclerView(ctx);
                rvGallery.setLayoutManager(new GridLayoutManager(ctx, 2));
                rvGallery.setAdapter(new LocalGalleryAdapter(images));

                return rvGallery;
        }

        public static View createGlobalGallery(ArrayList<FullAscii> asciis, Context ctx, WebsocetClient client, JSONObject queryParams) {

                if (asciis.isEmpty()) {
                        TextView tv = new TextView(ctx);
                        tv.setText("No image found");
                        return tv;
                }

                RecyclerView rvGallery = new RecyclerView(ctx);
                GlobalGalleryAdapter adapter = new GlobalGalleryAdapter(asciis);

                rvGallery.setLayoutManager(new GridLayoutManager(ctx, 2));
                rvGallery.setAdapter(adapter);

                final boolean[] isLoading = {false};

                rvGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                if (dy < 0 ) return;

                                GridLayoutManager layoutManager =
                                        (GridLayoutManager) recyclerView.getLayoutManager();

                                int lastVisible = layoutManager.findLastVisibleItemPosition();
                                int total = layoutManager.getItemCount();

                                final int PRELOAD_MARGIN = 2;

                                if (!isLoading[0] && lastVisible >= total - PRELOAD_MARGIN) {

                                        isLoading[0] = true;

                                        client.sendMessage(queryParams, msg -> {
                                                try {
                                                        JSONArray array = new JSONArray(msg);
                                                        ArrayList<FullAscii> newAsciis = FullAscii.fromJSONArray(array);

                                                        recyclerView.post(() -> {
                                                                adapter.addAsciis(newAsciis);
                                                                isLoading[0] = false;
                                                        });

                                                } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                }
                                        });
                                }
                        }
                });

                return rvGallery;
        }

}
