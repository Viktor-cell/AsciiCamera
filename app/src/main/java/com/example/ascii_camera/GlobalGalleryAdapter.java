package com.example.ascii_camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GlobalGalleryAdapter extends RecyclerView.Adapter<GlobalGalleryAdapter.GalleryViewHolder> {
        ArrayList<FullAscii> fullAsciis;

        public GlobalGalleryAdapter(ArrayList<FullAscii> fullAsciis) {
                this.fullAsciis = fullAsciis;
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

                holder.text.setText(fullAscii.getAuthor() + ":" + fullAscii.getArtName());
        }

        @Override
        public int getItemCount() {
                return fullAsciis.size();
        }

        public static class GalleryViewHolder extends RecyclerView.ViewHolder {
                private final Button button;
                private final TextView text;
                private final ImageView img;
                private final Context ctx;

                public GalleryViewHolder(@NonNull View itemView, Context ctx) {
                        super(itemView);
                        this.button = itemView.findViewById(R.id.button);
                        this.text = itemView.findViewById(R.id.text);
                        this.img = itemView.findViewById(R.id.image);
                        this.ctx = ctx;
                }
        }
}
