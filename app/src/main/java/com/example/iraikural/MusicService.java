package com.example.iraikural;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import java.io.IOException;

public class MusicService extends Service {
    private static final String CHANNEL_ID = "RadioChannel";
    private static final int NOTIFICATION_ID = 1;
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private boolean shouldResumeOnFocusGain = false;

    private static final String RADIO_URL = "https://usa14.fastcast4u.com/proxy/iraikuralfm?mp=/1";

    private final AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        pause();
                        unregisterMediaButton();
                        shouldResumeOnFocusGain = false;
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        pause();
                        shouldResumeOnFocusGain = isPlaying;
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        //
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        if (shouldResumeOnFocusGain) {
                            play();
                            shouldResumeOnFocusGain = false;
                        }
                        registerMediaButton();
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initializeMediaSession();
        createNotificationChannel();
        initMediaPlayer();
        // requestAudioFocus(); // Request focus only when playing
    }

    private void requestAudioFocus() {
        int result = audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("MusicService", "Audio focus granted");
            registerMediaButton();
        } else {
            Log.d("MusicService", "Audio focus not granted");
        }
    }

    private void registerMediaButton() {
        if (mediaSession != null) {
            audioManager.registerMediaButtonEventReceiver(
                    new ComponentName(getPackageName(), BluetoothReceiver.class.getName()));
            Log.d("MusicService", "Registered media button receiver");
        }
    }

    private void unregisterMediaButton() {
        if (mediaSession != null) {
            audioManager.unregisterMediaButtonEventReceiver(
                    new ComponentName(getPackageName(), BluetoothReceiver.class.getName()));
            Log.d("MusicService", "Unregistered media button receiver");
        }
    }

    private void initializeMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                Log.d("MusicService", "MediaSessionCompat: onPlay called");
                play();
            }

            @Override
            public void onPause() {
                Log.d("MusicService", "MediaSessionCompat: onPause called");
                pause();
            }

            @Override
            public void onStop() {
                Log.d("MusicService", "MediaSessionCompat: onStop called");
                stop();
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                Log.d("MusicService", "MediaSessionCompat: onMediaButtonEvent called");
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("MusicService", "Media button event: keyCode=" + event.getKeyCode());
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            play();
                            return true;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pause();
                            return true;
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            if (isPlaying) pause(); else play();
                            return true;
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(
                this, 0, new Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(
                        new ComponentName(getPackageName(), BluetoothReceiver.class.getName())),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(RADIO_URL);
            mediaPlayer.prepareAsync();
            Log.d("MusicService", "MediaPlayer preparing async");
            broadcastPlaybackState("preparing");
        } catch (IOException e) {
            Log.e("MusicService", "Error setting data source", e);
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            Log.d("MusicService", "MediaPlayer prepared");
            if (isPlaying) {
                mediaPlayer.start();
                showNotification();
                broadcastPlaybackState();
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            Log.d("MusicService", "MediaPlayer completed");
            stopSelf();
        });
    }

    public void play() {
        if (!isPlaying) {
            isPlaying = true;
            if (mediaPlayer == null) {
                initMediaPlayer();
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            updateMediaSessionState();
            showNotification();
            startForeground(NOTIFICATION_ID, buildNotification());
            broadcastPlaybackState();
            requestAudioFocus();
            shouldResumeOnFocusGain = false;
            Log.d("MusicService", "Play called, isPlaying=" + isPlaying);
        } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateMediaSessionState();
            showNotification();
            requestAudioFocus();
            shouldResumeOnFocusGain = false;
            broadcastPlaybackState();
            Log.d("MusicService", "Resumed playback, isPlaying=" + isPlaying);
        }
    }

    public void pause() {
        if (isPlaying) {
            isPlaying = false;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            updateMediaSessionState();
            showNotification();
            broadcastPlaybackState();
            abandonAudioFocus();
            unregisterMediaButton();
            Log.d("MusicService", "Pause called, isPlaying=" + isPlaying);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            isPlaying = false;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            updateMediaSessionState();
            stopForeground(true);
            stopSelf();
            broadcastPlaybackState();
            abandonAudioFocus();
            unregisterMediaButton();
            Log.d("MusicService", "Stop called, isPlaying=" + isPlaying);
        }
    }

    private void abandonAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(afChangeListener);
            Log.d("MusicService", "Audio focus abandoned");
        }
    }

    private void updateMediaSessionState() {
        mediaSession.setActive(isPlaying);
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_STOP)
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING :
                                PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build();
        mediaSession.setPlaybackState(state);
    }

    private void showNotification() {
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playIntent = new Intent(this, MusicActionReceiver.class);
        playIntent.setAction(isPlaying ? "com.example.iraikural.ACTION_PAUSE" : "com.example.iraikural.ACTION_PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, MusicActionReceiver.class);
        stopIntent.setAction("com.example.iraikural.ACTION_STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                isPlaying ? "Pause" : "Play",
                playPendingIntent
        );

        NotificationCompat.Action stopAction = new NotificationCompat.Action(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
        );

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "IRAIKURAL FM")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Live Radio")
                .build();
        mediaSession.setMetadata(metadata);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("IRAIKURAL FM")
                .setContentText("Playing Now")
                .setSmallIcon(R.drawable.ic_music_note)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1))
                .addAction(playPauseAction)
                .addAction(stopAction)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(isPlaying)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Playback",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void broadcastPlaybackState() {
        broadcastPlaybackState(null);
    }

    private void broadcastPlaybackState(String state) {
        Log.d("MusicService", "Broadcasting PLAYBACK_STATE: isPlaying=" + isPlaying + ", state=" + state);
        Intent intent = new Intent("com.example.iraikural.PLAYBACK_STATE");
        intent.setPackage(getPackageName());
        intent.putExtra("isPlaying", isPlaying);
        if (state != null) {
            intent.putExtra("state", state);
        }
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d("MusicService", "onStartCommand: action=" + action);
            if ("com.example.iraikural.PLAY".equals(action)) {
                play();
            } else if ("com.example.iraikural.PAUSE".equals(action)) {
                pause();
            } else if ("com.example.iraikural.ACTION_STOP".equals(action)) {
                stop();
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        abandonAudioFocus();
        unregisterMediaButton();
        Log.d("MusicService", "Service destroyed");
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}