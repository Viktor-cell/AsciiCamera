package com.example.ascii_camera;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class Gallery {
    public static ArrayList<Uri> findAll(Context ctx, String prefix) {
        ArrayList<Uri> uris = new ArrayList<>();

        ContentResolver contentResolver = ctx.getContentResolver();
        Uri where = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] what = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE
        };

        Cursor cursor = contentResolver.query(where, what, null, null, null);

        if (cursor == null) {
            Log.d("GALLERY_", "cursor == null");
        } else if (!cursor.moveToFirst()) {
            Log.d("GALLERY_", "!cursor.moveToFirst()");
        } else {
            int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
            int fullPathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                String thisTitle = cursor.getString(titleColumn);
                String thisPath = cursor.getString(fullPathColumn);

                if (thisTitle.toString().startsWith(prefix)) {
                    uris.add( Uri.fromFile(new File(thisPath)) );
                }
            } while (cursor.moveToNext());
        }
        Log.d("GALLERY_", "found: " + uris.size());
        return uris;
    }
}