package com.example.ascii_camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class Helper {
    public static void createTmpFolder(Context ctx, String name) {
        File dir = ctx.getCacheDir();
        File tmpImage = new File(dir + "/" + name);
        if (!tmpImage.exists()) tmpImage.mkdir();
    }

    public static void emptyTmpFolder(Context ctx, String name) {
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
    public static void getPermisions(Context ctx, Activity act) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
