package com.aseli.sholattimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_loading);
    }

    @Override
    protected void onStart(){
        super.onStart();
        new JadwalTask(this).execute();
    }

    public void startTimer(View v){
        Button button = (Button) v;
        button.setEnabled(false);
        Intent timer = new Intent(this, TimerService.class);

        if(isServiceRunning(TimerService.class)){
            Intent alarm = new Intent(this, AlarmService.class);
            stopService(alarm);
            stopService(timer);
            button.setText("Hidupkan Timer");
        } else {
            startService(timer);
            button.setText("Matikan Timer");
        }
        button.setEnabled(true);
    }
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}