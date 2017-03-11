package com.example.jay_pc.dcrypt;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Jay-pc on 3/10/2017.
 */

public class DCryptService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification note = new Notification(R.drawable.db_icon, "Started DCrypt", System.currentTimeMillis());

        Intent intent2 = new Intent(this, DCryptActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent2, 0);
        note.setLatestEventInfo(this, "DCrypt", "DCrypt running", pi);
        startForeground(1337, note);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}