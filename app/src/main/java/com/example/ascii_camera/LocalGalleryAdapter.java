package com.example.ascii_camera;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class LocalGalleryAdapter extends RecyclerView.Adapter<LocalGalleryAdapter.GalleryViewHolder> {
        ArrayList<Uri> imageUris;

        public LocalGalleryAdapter(ArrayList<Uri> imageUris) {
                this.imageUris = imageUris;
        }

        @NonNull
        @Override
        public LocalGalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.local_gallery_item_placeholder, parent, false);

                return new GalleryViewHolder(view, parent.getContext());
        }

        @Override
        public void onBindViewHolder(@NonNull LocalGalleryAdapter.GalleryViewHolder holder, int position) {
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

                holder.img.setOnClickListener(view -> {
                        LayoutInflater inflater = LayoutInflater.from(holder.ctx);

                        View customDialog = inflater.inflate(R.layout.image_preview_main_activity_dialog, null);

                        ImageView img = customDialog.findViewById(R.id.imgPreview);
                        Button bt = customDialog.findViewById(R.id.btClose);

                        AlertDialog alert = new AlertDialog.Builder(holder.ctx)
                                .setCancelable(true)
                                .setMessage(holder.tv.getText())
                                .setView(customDialog)
                                .create();

                        bt.setOnClickListener(v -> {
                                alert.dismiss();
                        });

                        img.setImageURI(imageUris.get(position));
                        alert.show();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                });
        }

        @Override
        public int getItemCount() {
                return imageUris.size();
        }

        public static class GalleryViewHolder extends RecyclerView.ViewHolder {
                ImageView img;
                TextView tv;
                Button btnTrash;
                //LinearLayout llTextAndButton;
                Context ctx;

                public GalleryViewHolder(@NonNull View itemView, Context ctx) {
                        super(itemView);
                        this.img = itemView.findViewById(R.id.image);
                        this.tv = itemView.findViewById(R.id.text);
                        this.btnTrash = itemView.findViewById(R.id.btnTrash);
                        this.ctx = ctx;
                }
        }
}
