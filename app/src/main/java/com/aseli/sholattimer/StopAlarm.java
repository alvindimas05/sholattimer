package com.aseli.sholattimer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class StopAlarm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        Intent alarm = new Intent(this, AlarmService.class);
        stopService(alarm);

        Intent timer = new Intent(this, TimerService.class);
        startService(timer);

        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
