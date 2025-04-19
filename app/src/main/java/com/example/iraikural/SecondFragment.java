package com.example.iraikural;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private ImageView imageView;
    private List<String> imageUrls = new ArrayList<>();
    private int currentImageIndex = 0;
    private Handler handler = new Handler();
    private Runnable imageSwitcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        // Find Views
        imageView = view.findViewById(R.id.imageLogo); // Ensure this ID matches your XML
        ImageView instagramIcon = view.findViewById(R.id.btnInstagram);
        ImageView facebookIcon = view.findViewById(R.id.btnFacebook);
        ImageView whatsappIcon = view.findViewById(R.id.btnWhatsApp);
        ImageView youtubeIcon = view.findViewById(R.id.btnYouTube);
        TextView websiteText = view.findViewById(R.id.textWebsite);
        TextView emailText = view.findViewById(R.id.textEmail);

        // Fetch images from Firebase
        fetchImageUrls();

        // Social Media Click Listeners
        instagramIcon.setOnClickListener(v -> openUrl("https://www.instagram.com/iraikuralfm?igsh=ejhidGVoNWh1dnpo"));
        facebookIcon.setOnClickListener(v -> openUrl("https://www.facebook.com/profile.php?id=61569826100992"));
        whatsappIcon.setOnClickListener(v -> openUrl("https://whatsapp.com/channel/0029Vb4DXEEInlqX2bVk5D3P"));
        youtubeIcon.setOnClickListener(v -> openUrl("https://www.youtube.com/@Iraikuralfm"));
        websiteText.setOnClickListener(v -> openUrl("https://www.iraikural.com"));
        emailText.setOnClickListener(v -> sendEmail("iraikuralmedia@gmail.com"));

        return view;
    }

    // Fetch images from Firebase
    private void fetchImageUrls() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("images");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> urls = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String url = child.child("url").getValue(String.class);
                    if (url != null) {
                        urls.add(url);
                    }
                }

                if (!urls.isEmpty()) {
                    Log.d("SecondFragment", "Fetched Image URLs: " + urls.toString());
                    imageUrls.clear();
                    imageUrls.addAll(urls);
                    currentImageIndex = 0;
                    loadImage(); // Load first image
                    startImageTimer(); // Start cycling images
                } else {
                    Log.e("SecondFragment", "No images found in Firebase!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SecondFragment", "Firebase error: " + error.getMessage());
            }
        });
    }

    // Load image using Glide
    private void loadImage() {
        if (imageUrls.isEmpty()) {
            Log.e("SecondFragment", "No images to load!");
            return;
        }

        String imageUrl = imageUrls.get(currentImageIndex);
        Log.d("SecondFragment", "Loading Image: " + imageUrl);

        Glide.with(requireContext())
                .load(imageUrl)
                .into(imageView);
    }

    // Cycle images every 4 seconds
    private void startImageTimer() {
        imageSwitcher = new Runnable() {
            @Override
            public void run() {
                if (!imageUrls.isEmpty()) {
                    currentImageIndex = (currentImageIndex + 1) % imageUrls.size();
                    loadImage();
                    handler.postDelayed(this, 4000); // Change image every 4 sec
                }
            }
        };
        handler.postDelayed(imageSwitcher, 4000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(imageSwitcher); // Stop image switching
    }

    // Open URL method
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // Send Email method
    private void sendEmail(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
}
