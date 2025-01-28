package com.example.iraikural;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Button playPauseButton;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playPauseButton = findViewById(R.id.playPauseButton);

        // Initialize MediaPlayer with your radio stream URL
        String radioStreamUrl = "https://usa14.fastcast4u.com/proxy/iraikuralfm?mp=/1"; // Replace with your URL
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(radioStreamUrl);
            mediaPlayer.prepareAsync(); // Prepare asynchronously to avoid blocking the main thread
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the play/pause button
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseRadio();
                } else {
                    playRadio();
                }
            }
        });

        // Set up MediaPlayer listeners
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // MediaPlayer is ready to play
                playPauseButton.setEnabled(true);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // Handle errors
                playPauseButton.setEnabled(false);
                return false;
            }
        });
    }

    private void playRadio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playPauseButton.setText("Pause");
            isPlaying = true;
        }
    }

    private void pauseRadio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
            isPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}