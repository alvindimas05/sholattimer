package com.aseli.sholattimer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class JadwalTask extends AsyncTask<Void, Void, Void>{
    public Context context;
    public JadwalTask(Context context){
        this.context = context;
    }
    @Override
    protected Void doInBackground(Void... voids){
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int month = c.get(Calendar.MONTH) + 1;
            String fileName = (month < 10 ? "0" : "") + month + ".json";

            File file = context.getFileStreamPath(fileName);
            if(file.exists()) return null;

            URL url = new URL("https://raw.githubusercontent.com/lakuapik/jadwalsholatorg/master/adzan/surakarta/2023/0" + month + ".json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.e("MainActivity", "Failed to connect to the server!");
                return null;
            }

            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            stream.close();

            String data = builder.toString();
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.w("Save File", e);
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

        Activity activity = (Activity) context;
        if(isServiceRunning(AlarmService.class)){
            Intent intent = new Intent(context, StopAlarm.class);
            activity.startActivity(intent);
            activity.finish();
        } else {
            activity.setContentView(R.layout.activity_main);
            try {
                startWaktuSholat();
            } catch (Exception e){
                Log.w("MainActivity", e);
            }
            if(isServiceRunning(TimerService.class)){
                Button btn = activity.findViewById(R.id.btn_timer);
                btn.setText("Matikan Timer");
            }
        }
    }
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void startWaktuSholat() throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int month = c.get(Calendar.MONTH) + 1;
        String fileName = (month < 10 ? "0" : "") + month + ".json";

        InputStream is = context.openFileInput(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;

        while((line = br.readLine()) != null){
            sb.append(line);
        }
        JSONArray data = new JSONArray(sb.toString());

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

        Activity activity = (Activity) context;
        LinearLayout parent = activity.findViewById(R.id.main_layout);
        int j = 0;
        for (int i = 0; i < obj.names().length(); i++) {
            String key = obj.names().getString(i);
            String[] list = {"tanggal", "imsyak", "terbit", "dhuha"};
            if (Arrays.asList(list).contains(key)) continue;

            CardView card = (CardView) parent.getChildAt(j);
            LinearLayout layout = (LinearLayout) card.getChildAt(0);

            ((TextView) layout.getChildAt(0)).setText(key.substring(0, 1).toUpperCase() + key.substring(1));
            ((TextView) layout.getChildAt(1)).setText(obj.getString(key));
            j++;
        }
    }
}