package com.example.iraikural;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdImageAdapter extends RecyclerView.Adapter<AdImageAdapter.AdViewHolder> {
    private Context context;
    private List<String> adImageUrls;

    public AdImageAdapter(Context context, List<String> adImageUrls) {
        this.context = context;
        this.adImageUrls = adImageUrls;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ad_image, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        String imageUrl = adImageUrls.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageViewAd);
    }

    @Override
    public int getItemCount() {
        return adImageUrls.size();
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAd;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAd = itemView.findViewById(R.id.imageViewAd);
        }
    }
}
