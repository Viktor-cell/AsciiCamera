package com.example.ascii_camera;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                Uri currentUri = imageUris.get(adapterPosition);
                                File f = new File(currentUri.getPath());
                                f.delete();

                                imageUris.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                        }
                });

                holder.img.setOnClickListener(view -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                LayoutInflater inflater = LayoutInflater.from(holder.ctx);

                                View customDialog = inflater.inflate(R.layout.image_preview_main_activity_dialog, null);

                                ImageView img = customDialog.findViewById(R.id.imgPreview);
                                ImageButton bt = customDialog.findViewById(R.id.btClose);

                                AlertDialog alert = new AlertDialog.Builder(holder.ctx)
                                        .setCancelable(true)
                                        .setView(customDialog)
                                        .create();

                                bt.setOnClickListener(v -> {
                                        alert.dismiss();
                                });

                                img.setImageURI(imageUris.get(adapterPosition));
                                alert.show();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        }
                });
        }

        @Override
        public int getItemCount() {
                return imageUris.size();
        }

        public static class GalleryViewHolder extends RecyclerView.ViewHolder {
                ImageView img;
                TextView tv;
                ImageButton btnTrash;
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