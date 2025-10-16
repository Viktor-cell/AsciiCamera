package com.example.ascii_camera;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    ArrayList<Uri> imageUris;

    @NonNull
    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView img = new ImageView(parent.getContext());
        TextView tv = new TextView(parent.getContext());
        Button btnTrash = new Button(parent.getContext());
        LinearLayout llTextAndButton = new LinearLayout(parent.getContext());
        LinearLayout llMain = new LinearLayout(parent.getContext());

        llMain.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                800
        ));
        llMain.setOrientation(LinearLayout.VERTICAL);

        llTextAndButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        img.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);

        btnTrash.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        btnTrash.setGravity(Gravity.CENTER);
        btnTrash.setText("X");
        btnTrash.setTextColor(Color.WHITE);
        btnTrash.setBackgroundColor(Color.RED);

        llTextAndButton.addView(tv);
        llTextAndButton.addView(btnTrash);

        llMain.addView(img);
        llMain.addView(llTextAndButton);

        return new GalleryViewHolder(llMain, llTextAndButton, img, tv, btnTrash, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.GalleryViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        String uriPath = uri.getLastPathSegment();
        holder.img.setImageURI(uri);
        holder.tv.setText(uriPath.substring(6, uriPath.length() - 4));
        holder.btnTrash.setOnClickListener(view -> {
            File f = new File(uri.getPath());
            
            f.delete();
            imageUris.remove(position);
            notifyItemRemoved(position);
        });
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
        Button btnTrash;
        LinearLayout llTextAndButton;

        Context ctx;

        public GalleryViewHolder(@NonNull View itemView, LinearLayout llTextAndButton, ImageView img, TextView tv, Button btnTrash, Context ctx) {
            super(itemView);
            this.img = img;
            this.tv = tv;
            this.llTextAndButton = llTextAndButton;
            this.btnTrash = btnTrash;
            this.ctx = ctx;
        }
    }
}
