package com.aseli.sholattimer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimerService extends Service {
    @Override
    public IBinder onBind(Intent i){
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        int month = c.get(Calendar.MONTH) + 1;
                        String fileName = (month < 10 ? "0" : "") + month + ".json";

                        JSONArray data = null;
                        InputStream is = openFileInput(fileName);
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String line;

                        while((line = br.readLine()) != null){
                            sb.append(line);
                        }
                        data = new JSONArray(sb.toString());

                        JSONObject obj = null;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        for(int i = 0; i < data.length(); i++){
                            obj = data.getJSONObject(i);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                            if(obj.getString("tanggal").equals(dateFormat.format(cal.getTime()))){
                                break;
                            }
                        }
                        for (int i = 0; i < obj.names().length(); i++) {
                            String key = obj.names().getString(i);
                            String[] list = {"tanggal", "imsyak", "terbit", "dhuha"};
                            if(Arrays.asList(list).contains(key)) continue;

                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
                            Date time = dateFormat.parse(obj.getString(key));

                            long seconds = (time.getTime() - dateFormat.parse(dateFormat.format(new Date())).getTime()) / 1000L;
                            if((int) seconds == 0){
                                int id = (int) System.currentTimeMillis();

                                Intent intent = new Intent(TimerService.this, StopAlarm.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pintent = PendingIntent.getActivity(TimerService.this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Builder notif = new NotificationCompat.Builder(TimerService.this, "sholattimer")
                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                        .setContentTitle("Sholat Timer")
                                        .setContentText("Sudah waktunya sholat")
                                        .setContentIntent(pintent)
                                        .setOngoing(true);

                                NotificationManager manager = (NotificationManager) getSystemService(TimerService.this.NOTIFICATION_SERVICE);
                                manager.notify(id, notif.build());

                                Intent alarm = new Intent(TimerService.this, AlarmService.class);
                                startService(alarm);
                                stopSelf();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.w("Check Date", e);
                    }
                }
            }
        });
        thread.start();
        return START_STICKY;
    }
}
