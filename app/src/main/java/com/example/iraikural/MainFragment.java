package com.example.iraikural;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import java.io.IOException;

public class MainFragment extends Fragment {

    private final String radioStreamUrl = "https://usa14.fastcast4u.com/proxy/iraikuralfm?mp=/1";
    private MediaPlayer mediaPlayer;
    private VisualizerView visualizerView;
    private Visualizer visualizer;
    private Button btnPlayPause;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        visualizerView = view.findViewById(R.id.visualizer);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);

        // Set up MediaPlayer with the radio stream URL
        setupMediaPlayer();

        // Play/Pause button logic
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    pausePlayback();
                } else {
                    startPlayback();
                }
            }
        });

        return view;
    }

    private void setupMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(radioStreamUrl); // Use the URL here
            mediaPlayer.prepareAsync(); // Prepare asynchronously for streaming

            // Set up listeners for MediaPlayer
            mediaPlayer.setOnPreparedListener(mp -> {
                btnPlayPause.setEnabled(true); // Enable the play button when MediaPlayer is prepared
                btnPlayPause.setText("Play"); // Set button text to "Play"
                Log.d("MainFragment", "MediaPlayer is prepared and ready");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MainFragment", "MediaPlayer error: what=" + what + ", extra=" + extra);
                return false;
            });
        } catch (IOException e) {
            Log.e("MainFragment", "Error setting up MediaPlayer", e);
        }
    }

    private void startPlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btnPlayPause.setText("Pause");
            setupVisualizer(); // Re-initialize visualizer when playback starts
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setText("Play");
            releaseVisualizer(); // Release visualizer when playback is paused
        }
    }

    private void setupVisualizer() {
        if (mediaPlayer != null) {
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1) {
                releaseVisualizer(); // Release existing visualizer before creating a new one
                visualizer = new Visualizer(audioSessionId);
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                visualizer.setDataCaptureListener(
                        new Visualizer.OnDataCaptureListener() {
                            @Override
                            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                                visualizerView.updateVisualizer(waveform);
                            }

                            @Override
                            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                                // Optional: Handle FFT data
                            }
                        },
                        Visualizer.getMaxCaptureRate() / 2,
                        true,
                        false
                );
                visualizer.setEnabled(true);
            }
        }
    }

    private void releaseVisualizer() {
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        releaseVisualizer();
    }
}