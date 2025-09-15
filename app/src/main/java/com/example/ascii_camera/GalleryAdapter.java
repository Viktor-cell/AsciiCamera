package com.example.ascii_camera;


import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    ArrayList<Uri> imageUris;

    @NonNull
    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView img = new ImageView(parent.getContext());
        TextView tv = new TextView(parent.getContext());
        LinearLayout ll = new LinearLayout(parent.getContext());

        ll.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                800
        ));
        ll.setOrientation(LinearLayout.VERTICAL);

        img.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);

        ll.addView(img);
        ll.addView(tv);

        return new GalleryViewHolder(ll, img, tv);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.GalleryViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        String uriPath = uri.getLastPathSegment();
        holder.img.setImageURI(uri);
        holder.tv.setText(uriPath.substring(6, uriPath.length() - 4));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public GalleryAdapter(ArrayList<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tv;

        public GalleryViewHolder(@NonNull View itemView, ImageView img, TextView tv) {
            super(itemView);
            this.img = img;
            this.tv = tv;
        }
    }
}
