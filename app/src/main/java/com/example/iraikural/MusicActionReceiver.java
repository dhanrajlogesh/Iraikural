package com.example.iraikural;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("MusicActionReceiver", "Received action: " + action);
        Intent serviceIntent = new Intent(context, MusicService.class);

        if ("com.example.iraikural.ACTION_PLAY".equals(action)) {
            serviceIntent.setAction("com.example.iraikural.PLAY");
        } else if ("com.example.iraikural.ACTION_PAUSE".equals(action)) {
            serviceIntent.setAction("com.example.iraikural.PAUSE");
        } else if ("com.example.iraikural.ACTION_STOP".equals(action)) {
            serviceIntent.setAction("com.example.iraikural.ACTION_STOP");
        }

        context.startService(serviceIntent);
    }
}