package com.example.emav1.toolspack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.emav1.R;

public class NotificationHandler {
    Context context;

    NotificationHandler (Context context){ // to be added the name, number and message
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        createNotifBuilder();
    }

    private void createNotifBuilder(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Default");
        builder.setContentTitle("EMA MESSAGE");
        builder.setContentText("Testing This is a Message!");
        builder.setSmallIcon(R.drawable.icon_ema);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());

    }





}
