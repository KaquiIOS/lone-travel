package com.example.collpasingtest.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.collpasingtest.R;
import com.example.collpasingtest.views.MainActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Notification {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private String title;

    private final String CHANNEL_ID = "FIREBASE_MSG_CHANNEL_02";

    public Notification(Context context) {
        this.mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 26 에서 달라진 점
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID).setAutoCancel(true);
        }else{
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
        }

        mNotifyBuilder.setVibrate(new long[]{1000,1000});
        mNotifyBuilder.setPriority(100);
        mNotifyBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mNotifyBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

    }

    public Notification setTitle(String title){
        mNotifyBuilder.setContentTitle(title);
        mNotifyBuilder.setTicker(title);
        this.title = title;
        return this;
    }

    public Notification setText(String text){
        mNotifyBuilder.setContentText(text);
        return this;
    }

    public Notification setData(Intent intent) {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent
                = taskStackBuilder.getPendingIntent(140, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyBuilder.setContentIntent(pendingIntent);
        return this;
    }

    public void notification() {

        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = title;
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            mNotificationManager.notify(1, mNotifyBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}