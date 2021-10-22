package com.inkhornsolutions.foodbox.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.inkhornsolutions.foodbox.R;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;

public class Common {

    public static ArrayList<String> id = new ArrayList<>();
    public static HashMap<String, Object> discountAvailable = new HashMap<>();
    public static ArrayList<String> res = new ArrayList<>();

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {

        PendingIntent pendingIntent = null;

        Log.d("message", "Chala");

        if (intent != null){
            Log.d("TAG", "Intent is not null");

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            pendingIntent = pendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = "1002";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "FoodBox", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("FoodBox");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationChannel.setSound(soundUri, audioAttributes);

                notificationManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                    .setSmallIcon(R.drawable.food_box_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.food_box_icon))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

            if (pendingIntent != null){
                builder.setContentIntent(pendingIntent);
            }
            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        }
        else {
            Log.d("TAG", "Intent is null");
        }
    }
}
