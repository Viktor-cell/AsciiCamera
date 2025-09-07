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
    private ArrayList<Uri> photos;
    private String photoPrefix;

    public Gallery(String photoPrefix, Context ctx) {
        this.photoPrefix = photoPrefix;
        findAll(ctx);
    }

    public ArrayList<Uri> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Uri> photos) {
        this.photos = photos;
    }

    public String getPhotoPrefix() {
        return photoPrefix;
    }

    public void setPhotoPrefix(String photoPrefix) {
        this.photoPrefix = photoPrefix;
    }

    public void findAll(Context ctx) {
        ArrayList<Uri> uris = new ArrayList<>();

        ContentResolver contentResolver = ctx.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor == null) {
            Log.d("gallery", "cursor == null");
        } else if (!cursor.moveToFirst()) {
            Log.d("gallery", "!cursor.moveToFirst()");
        } else {
            int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
            int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int fullPathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            do {

                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisPath = cursor.getString(fullPathColumn);

                if (thisTitle.toString().startsWith(this.photoPrefix)) {
                    uris.add( Uri.fromFile(new File(thisPath)) );
                }
            } while (cursor.moveToNext());
        }
        Log.d("gallery", "found: " + uris.size());
        setPhotos(uris);
    }
}
