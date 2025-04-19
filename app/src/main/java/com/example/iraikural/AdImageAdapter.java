package com.example.iraikural;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.iraikural.R;
import java.util.List;

public class AdImageAdapter extends RecyclerView.Adapter<AdImageAdapter.ViewHolder> {
    private Context context;
    private List<String> adImages;  // List of image URLs

    public AdImageAdapter(Context context, List<String> adImages) {
        this.context = context;
        this.adImages = adImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ad_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = adImages.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageViewAd);
    }

    @Override
    public int getItemCount() {
        return adImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAd = itemView.findViewById(R.id.imageViewAd);
        }
    }
}
