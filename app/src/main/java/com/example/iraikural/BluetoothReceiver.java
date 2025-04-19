package com.example.iraikural;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class BluetoothReceiver extends BroadcastReceiver {
    private static BluetoothListener listener;

    public static void setBluetoothListener(BluetoothListener bluetoothListener) {
        listener = bluetoothListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (listener != null) {
                        listener.onBluetoothMediaButtonPressed();
                    }
                    abortBroadcast(); // Prevent other apps from receiving this event
                }
            }
        }
    }

    public interface BluetoothListener {
        void onBluetoothMediaButtonPressed();
    }
}