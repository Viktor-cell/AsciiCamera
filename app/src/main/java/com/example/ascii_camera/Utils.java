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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
                                        Intent intent = new Intent(ctx, MainActivityLocalGallery.class);
                                        act.startActivity(intent);
                                }
                        }
                });
        }

        public static Uri saveLocaly(String fileName, Bitmap bmp, Context ctx) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "ascii_" + fileName + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                ContentResolver resolver = ctx.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try (OutputStream out = resolver.openOutputStream(uri)) {
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (IOException e) {
                        resolver.delete(uri, null, null);
                        throw new RuntimeException(e);
                }

                return uri;
        }

        public static void showGlobalGallery(ViewGroup layout, WebsocetClient client, Activity activity, JSONObject queryParams, boolean showAuthor) {
                ServerUtils.isOnlineAsync(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull java.io.IOException e) {
                                activity.runOnUiThread(() -> {
                                        layout.removeAllViews();
                                        View offlineView = createServerOfflineView(activity);
                                        layout.addView(offlineView);
                                });
                        }

                        @Override
                        public void onResponse(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull okhttp3.Response response) {
                                if (!response.isSuccessful()) {
                                        activity.runOnUiThread(() -> {
                                                layout.removeAllViews();
                                                View offlineView = createServerOfflineView(activity);
                                                layout.addView(offlineView);
                                        });
                                        return;
                                }

                                try {
                                        client.sendMessage(queryParams, msg -> {
                                                try {
                                                        JSONArray jsonArray = new JSONArray(msg);
                                                        ArrayList<FullAscii> fullAsciis = FullAscii.fromJSONArray(jsonArray);

                                                        activity.runOnUiThread(() -> {
                                                                layout.removeAllViews();
                                                                View galleryView = Utils.createGlobalGallery(fullAsciis, activity, client, queryParams, showAuthor);
                                                                layout.addView(galleryView);
                                                        });
                                                } catch (Exception e) {
                                                        activity.runOnUiThread(() -> {
                                                                layout.removeAllViews();
                                                                View errorView = createServerOfflineView(activity);
                                                                layout.addView(errorView);
                                                        });
                                                }
                                        });
                                } catch (Exception e) {
                                        activity.runOnUiThread(() -> {
                                                layout.removeAllViews();
                                                View errorView = createServerOfflineView(activity);
                                                layout.addView(errorView);
                                        });
                                }
                        }
                });
        }

        private static View createServerOfflineView(Context ctx) {
                android.widget.LinearLayout container = new android.widget.LinearLayout(ctx);
                container.setOrientation(android.widget.LinearLayout.VERTICAL);
                container.setGravity(android.view.Gravity.CENTER);
                container.setPadding(40, 40, 40, 40);

                ImageView iv = new ImageView(ctx);
                iv.setImageResource(R.drawable.ic_no_image);
                android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(
                        400, 400
                );
                ivParams.gravity = android.view.Gravity.CENTER;
                ivParams.setMargins(0, 0, 0, 20);
                iv.setLayoutParams(ivParams);

                TextView tv = new TextView(ctx);
                tv.setText("Server is offline\nPlease check your connection");
                tv.setTextSize(18);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setTextColor(0xFF666666);
                android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                tvParams.setMargins(0, 20, 0, 0);
                tv.setLayoutParams(tvParams);

                container.addView(iv);
                container.addView(tv);

                return container;
        }


        public static String createTmpFolder(Context ctx, String name) {
                File dir = ctx.getCacheDir();
                File tmpImage = new File(dir + "/" + name);
                if (!tmpImage.exists()) tmpImage.mkdir();
                return tmpImage.getPath().toString();
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
                        // Create a LinearLayout to hold the icon and text vertically
                        android.widget.LinearLayout container = new android.widget.LinearLayout(ctx);
                        container.setOrientation(android.widget.LinearLayout.VERTICAL);
                        container.setGravity(android.view.Gravity.CENTER);
                        container.setPadding(40, 40, 40, 40);

                        ImageView iv = new ImageView(ctx);
                        iv.setImageResource(R.drawable.ic_no_image);
                        android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(
                                400, 400
                        );
                        ivParams.gravity = android.view.Gravity.CENTER;
                        ivParams.setMargins(0, 0, 0, 20);
                        iv.setLayoutParams(ivParams);

                        TextView tv = new TextView(ctx);
                        tv.setText("No images saved yet\nCreate your first ASCII art!");
                        tv.setTextSize(18);
                        tv.setGravity(android.view.Gravity.CENTER);
                        tv.setTextColor(0xFF666666);
                        android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        tvParams.setMargins(0, 20, 0, 0);
                        tv.setLayoutParams(tvParams);

                        container.addView(iv);
                        container.addView(tv);

                        return container;
                }
                RecyclerView rvGallery = new RecyclerView(ctx);
                rvGallery.setLayoutManager(new GridLayoutManager(ctx, 2));
                rvGallery.setAdapter(new LocalGalleryAdapter(images));

                return rvGallery;
        }

        public static View createGlobalGallery(ArrayList<FullAscii> asciis, Context ctx, WebsocetClient client, JSONObject queryParams, boolean showAuthor) {

                if (asciis.isEmpty()) {
                        android.widget.LinearLayout container = new android.widget.LinearLayout(ctx);
                        container.setOrientation(android.widget.LinearLayout.VERTICAL);
                        container.setGravity(android.view.Gravity.CENTER);
                        container.setPadding(40, 40, 40, 40);

                        ImageView iv = new ImageView(ctx);
                        iv.setImageResource(R.drawable.ic_no_image);
                        android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(
                                400, 400
                        );
                        ivParams.gravity = android.view.Gravity.CENTER;
                        ivParams.setMargins(0, 0, 0, 20);
                        iv.setLayoutParams(ivParams);

                        TextView tv = new TextView(ctx);
                        tv.setText("No art found\nTry a different search or upload your own!");
                        tv.setTextSize(18);
                        tv.setGravity(android.view.Gravity.CENTER);
                        tv.setTextColor(0xFF666666);
                        android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        tvParams.setMargins(0, 20, 0, 0);
                        tv.setLayoutParams(tvParams);

                        container.addView(iv);
                        container.addView(tv);

                        return container;
                }

                RecyclerView rvGallery = new RecyclerView(ctx);
                GlobalGalleryAdapter adapter = new GlobalGalleryAdapter(asciis, showAuthor);

                rvGallery.setLayoutManager(new GridLayoutManager(ctx, 2));
                rvGallery.setAdapter(adapter);

                final boolean[] isLoading = {false};
                final boolean[] hasMoreData = {true};

                rvGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                if (dy < 0) return;

                                if (!hasMoreData[0]) return;

                                GridLayoutManager layoutManager =
                                        (GridLayoutManager) recyclerView.getLayoutManager();

                                int lastVisible = layoutManager.findLastVisibleItemPosition();
                                int total = layoutManager.getItemCount();

                                final int PRELOAD_MARGIN = 4;

                                if (!isLoading[0] && lastVisible >= total - PRELOAD_MARGIN) {

                                        isLoading[0] = true;

                                        client.sendMessage(queryParams, msg -> {
                                                try {
                                                        JSONArray array = new JSONArray(msg);
                                                        ArrayList<FullAscii> newAsciis = FullAscii.fromJSONArray(array);

                                                        recyclerView.post(() -> {
                                                                if (newAsciis.isEmpty()) {
                                                                        hasMoreData[0] = false;
                                                                } else {
                                                                        adapter.addAsciis(newAsciis);
                                                                }
                                                                isLoading[0] = false;
                                                        });

                                                } catch (Exception e) {
                                                        recyclerView.post(() -> {
                                                                isLoading[0] = false;
                                                        });
                                                        throw new RuntimeException(e);
                                                }
                                        });
                                }
                        }
                });

                return rvGallery;
        }
}