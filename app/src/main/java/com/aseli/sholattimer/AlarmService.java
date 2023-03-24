package com.aseli.sholattimer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AlarmService extends Service {
    MediaPlayer player;
    @Override
    public IBinder onBind(Intent i){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.heheheha);
        player.setLooping(true);
        player.setVolume(100, 100);
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        player.start();
        return startId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
    }
}
