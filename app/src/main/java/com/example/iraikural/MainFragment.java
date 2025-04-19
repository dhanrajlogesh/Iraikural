package com.example.iraikural;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Random;

public class MainFragment extends Fragment implements BluetoothReceiver.BluetoothListener {

    private Button btnPlayPause;
    private ImageView partnerAdImage;
    private DatabaseReference databaseReference;
    private List<String> adImageUrls = new ArrayList<>();
    private Handler adHandler = new Handler();
    private int currentAdIndex = 0;

    private WaveformView waveformView;
    private Handler waveformHandler = new Handler();
    private Random random = new Random();

    private MusicService musicService;
    private boolean isServiceBound = false;

    // ✅ BroadcastReceiver to listen for play/pause updates from MusicService
    private final BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.iraikural.PLAYBACK_STATE".equals(intent.getAction())) {
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                Log.d("MainFragment", "Received PLAYBACK_STATE broadcast: " + isPlaying);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isPlaying) {
                            btnPlayPause.setText("Pause");
                            startWaveformAnimation();
                        } else {
                            btnPlayPause.setText("Play Radio");
                            waveformHandler.removeCallbacksAndMessages(null);
                            setDefaultWaveform();
                        }
                    });
                }
            }
        }
    };


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            updatePlayPauseButton(); // ✅ Ensure correct button state on bind
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            musicService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        partnerAdImage = view.findViewById(R.id.partnerAdImage);
        waveformView = view.findViewById(R.id.waveformView);

        setDefaultWaveform();
        fetchPartnerAds();

        // Start and bind to the MusicService
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        btnPlayPause.setOnClickListener(v -> togglePlayback());

        // Register Bluetooth listener
        BluetoothReceiver.setBluetoothListener(this);

        // ✅ Register broadcast receiver here
        IntentFilter intentFilter = new IntentFilter("com.example.iraikural.PLAYBACK_STATE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requireContext().registerReceiver(playbackStateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(playbackStateReceiver, intentFilter);
        }

        return view;
    }

    private void togglePlayback() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pause();
            } else {
                musicService.play();
            }
            updatePlayPauseButton();
        } else {
            Toast.makeText(getContext(), "Service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlayPauseButton() {
        if (musicService != null && musicService.isPlaying()) {
            btnPlayPause.setText("Pause");
            startWaveformAnimation();
        } else {
            btnPlayPause.setText("Play Radio");
            waveformHandler.removeCallbacksAndMessages(null);
            setDefaultWaveform();
        }
    }

    private void startWaveformAnimation() {
        if (waveformView == null) return;

        waveformHandler.post(new Runnable() {
            @Override
            public void run() {
                if (musicService != null && musicService.isPlaying()) {
                    waveformView.setWaveform(generateRandomWaveform());
                    Log.d("WaveformView", "Waveform updated!");
                    waveformHandler.postDelayed(this, 100);
                }
            }
        });
    }

    private List<Float> generateRandomWaveform() {
        List<Float> waveform = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            waveform.add(60 + random.nextFloat() * 180);
        }
        return waveform;
    }

    private void setDefaultWaveform() {
        List<Float> defaultWaveform = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            defaultWaveform.add(100f);
        }
        waveformView.setWaveform(defaultWaveform);
    }

    private void fetchPartnerAds() {
        databaseReference = FirebaseDatabase.getInstance("https://iraikuralfm-partners.firebaseio.com/")
                .getReference("partners");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adImageUrls.clear();
                for (DataSnapshot partnerSnapshot : snapshot.getChildren()) {
                    String imageUrl = partnerSnapshot.child("url").getValue(String.class);
                    if (imageUrl != null) {
                        adImageUrls.add(imageUrl);
                    }
                }
                if (!adImageUrls.isEmpty()) {
                    startAdImageTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainFragment", "Firebase error: " + error.getMessage());
            }
        });
    }

    private void startAdImageTimer() {
        adHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!adImageUrls.isEmpty() && partnerAdImage != null) {
                    Glide.with(requireContext())
                            .load(adImageUrls.get(currentAdIndex))
                            .into(partnerAdImage);

                    currentAdIndex = (currentAdIndex + 1) % adImageUrls.size();
                }
                adHandler.postDelayed(this, 4000);
            }
        }, 4000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (isServiceBound) {
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }

        try {
            getActivity().unregisterReceiver(playbackStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.w("MainFragment", "Receiver already unregistered");
        }

        adHandler.removeCallbacksAndMessages(null);
        waveformHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBluetoothMediaButtonPressed() {
        //togglePlayback();
    }
}
