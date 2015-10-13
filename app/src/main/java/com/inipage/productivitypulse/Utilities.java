package com.inipage.productivitypulse;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class Utilities {
    public static void sendVibrate(Context context, int intensity, int count, int timeOn){
        Intent vibrateIntent = new Intent(Constants.VIBRATE_REQUEST);
        vibrateIntent.putExtra(Constants.INTENSITY, intensity);
        vibrateIntent.putExtra(Constants.COUNT, count);
        vibrateIntent.putExtra(Constants.ON_TIME, timeOn);
        vibrateIntent.putExtra(Constants.OFF_TIME, 500);
        vibrateIntent.putExtra(Constants.IDENTITY, PendingIntent.getActivity(context,
                0, new Intent(context, MainActivity.class), 0));
        context.sendBroadcast(vibrateIntent);
    }

    public static void sendShock(Context context, int intensity){
        Intent shockIntent = new Intent(Constants.ZAP_REQUEST);
        shockIntent.putExtra(Constants.INTENSITY, intensity);
        shockIntent.putExtra(Constants.ON_TIME, 500);
        shockIntent.putExtra(Constants.OFF_TIME, 500);
        shockIntent.putExtra(Constants.COUNT, 1);
        shockIntent.putExtra(Constants.IDENTITY, PendingIntent.getActivity(context,
                0, new Intent(context, MainActivity.class), 0));
        context.sendBroadcast(shockIntent);
    }

    public static void sendNotification(Context context, String title, String message){
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(Constants.NOTIFICATION_ID,
                new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setShowWhen(false)
                    .setSmallIcon(R.drawable.ic_access_time_white_24dp)
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0))
                    .build());
    }

    public static void saveAuthToken(Context context, String token){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.AUTH_TOKEN_PREF, token).apply();
    }

    public static String getAuthToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.AUTH_TOKEN_PREF, "");
    }
}
