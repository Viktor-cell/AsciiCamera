package com.example.ascii_camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class GlobalGalleryAdapter extends RecyclerView.Adapter<GlobalGalleryAdapter.GalleryViewHolder> {
        ArrayList<FullAscii> fullAsciis;

        public GlobalGalleryAdapter(ArrayList<FullAscii> fullAsciis) {
                this.fullAsciis = fullAsciis;
        }

        public GlobalGalleryAdapter() {
                fullAsciis = new ArrayList<>();
        }

        public void addAsciis(ArrayList<FullAscii> fullAsciis) {
                int oldSize = this.fullAsciis.size();
                this.fullAsciis.addAll(fullAsciis);
                notifyItemRangeInserted(oldSize, fullAsciis.size());
        }

        @NonNull
        @Override
        public GlobalGalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.global_gallery_item_placeholder, parent, false);

                return new GalleryViewHolder(view, parent.getContext());
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
                FullAscii fullAscii = fullAsciis.get(position);
                holder.img.setImageBitmap(fullAscii.toAsciiBitmap(holder.ctx));

                holder.tvAuthor.setText(fullAscii.getAuthor());
                holder.tvArtname.setText(fullAscii.getArtName());

                holder.img.setOnClickListener(view -> {
                        LayoutInflater inflater = LayoutInflater.from(holder.ctx);

                        View customDialog = inflater.inflate(R.layout.image_preview_main_activity_dialog, null);

                        ImageView img = customDialog.findViewById(R.id.imgPreview);
                        ImageButton bt = customDialog.findViewById(R.id.btClose);

                        AlertDialog alert = new AlertDialog.Builder(holder.ctx)
                                .setCancelable(true)
                                //.setMessage(holder.tvArtname.getText())
                                .setView(customDialog)
                                .create();

                        bt.setOnClickListener(v -> {
                                alert.dismiss();
                        });

                        img.setImageBitmap(fullAsciis.get(position).toAsciiBitmap(holder.ctx));
                        alert.show();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                });

                holder.button.setOnClickListener(view -> {
                        Utils.saveLocaly(holder.tvArtname.getText().toString(), fullAsciis.get(position).toAsciiBitmap(holder.ctx), holder.ctx);
                        Toast.makeText(holder.ctx, "image saved", Toast.LENGTH_SHORT).show();
                });
        }

        @Override
        public int getItemCount() {
                return fullAsciis.size();
        }

        public static class GalleryViewHolder extends RecyclerView.ViewHolder {
                private final MaterialButton button;
                private final TextView tvAuthor;
                private final TextView tvArtname;
                private final ImageView img;
                private final Context ctx;

                public GalleryViewHolder(@NonNull View itemView, Context ctx) {
                        super(itemView);
                        this.button = itemView.findViewById(R.id.button);
                        this.tvAuthor = itemView.findViewById(R.id.tvAuthor);
                        this.tvArtname = itemView.findViewById(R.id.artName);
                        this.img = itemView.findViewById(R.id.image);
                        this.ctx = ctx;
                }
        }
}
