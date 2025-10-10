package com.example.ascii_camera;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.security.MessageDigest;

public class Utils {

    public static final String LOGGED_OUT_USERNAME = "anonymous";

    public static void createTmpFolder(Context ctx, String name) {
        File dir = ctx.getCacheDir();
        File tmpImage = new File(dir + "/" + name);
        if (!tmpImage.exists()) tmpImage.mkdir();
    }

    public static void cleanTmpFolder(Context ctx, String name) {
        File dir = new File(ctx.getCacheDir() + "/" + name);
        if (!dir.exists()) {
            Log.d("DIR_", "dir doesnt exists, cant remove anything");
            return;
        } else if (dir.listFiles().length == 0) {
            Log.d("DIR_", "empty dir, cant remove anything");
            return;
        }
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    public static void showContentOfTmpFolder(Context ctx, String name) {
        File dir = new File(ctx.getCacheDir() + "/" + name);
        if (!dir.exists()) {
            Log.d("DIR_", "dir doesnt exists, cant show anything");
            return;
        } else if (dir.listFiles().length == 0) {
            Log.d("DIR_", "empty dir, cant show anything");
            return;
        }
        for (File file : dir.listFiles()) {
            Log.d("DIR_", file.toString());
        }
    }

    public static void getPermissions(Context ctx, Activity act) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private static final String PREFS_LOCATION = "user_data";

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
}
