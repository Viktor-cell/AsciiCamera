package com.example.ascii_camera;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GlobalGalleryAdapter extends RecyclerView.Adapter<GlobalGalleryAdapter.GalleryViewHolder> {
        @NonNull
        @Override
        public GlobalGalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
                return 0;
        }

        public static class GalleryViewHolder extends RecyclerView.ViewHolder {

                public GalleryViewHolder(@NonNull View itemView) {
                        super(itemView);
                }
        }
}
