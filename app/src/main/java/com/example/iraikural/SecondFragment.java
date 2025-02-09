package com.example.iraikural;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        // Find Views
        ImageView instagramIcon = view.findViewById(R.id.btnInstagram);
        ImageView facebookIcon = view.findViewById(R.id.btnFacebook);
        ImageView whatsappIcon = view.findViewById(R.id.btnWhatsApp);
        ImageView youtubeIcon = view.findViewById(R.id.btnYouTube);
        TextView websiteText = view.findViewById(R.id.textWebsite);
        TextView emailText = view.findViewById(R.id.textEmail);

        // Instagram Click Listener
        instagramIcon.setOnClickListener(v -> openUrl("https://www.instagram.com/iraikuralfm?igsh=ejhidGVoNWh1dnpo"));

        // Facebook Click Listener
        facebookIcon.setOnClickListener(v -> openUrl("https://www.facebook.com/profile.php?id=61569826100992 "));

        // WhatsApp Click Listener
        whatsappIcon.setOnClickListener(v -> openUrl("https://wa.me/+4917613640046"));

        // YouTube Click Listener
        youtubeIcon.setOnClickListener(v -> openUrl("https://www.youtube.com/@Iraikuralfm"));

        // Website Click Listener
        websiteText.setOnClickListener(v -> openUrl("https://www.iraikural.com"));

        // Email Click Listener
        emailText.setOnClickListener(v -> sendEmail("iraikuralmedia@gmail.com"));

        return view;
    }

    // Method to open URLs
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // Method to send email
    private void sendEmail(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
}
