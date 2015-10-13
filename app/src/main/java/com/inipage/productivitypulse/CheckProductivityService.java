package com.inipage.productivitypulse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.internal.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

import retrofit.Callback;
import retrofit.Response;

public class CheckProductivityService extends Service {
    private static final String TAG = "CheckProductivityServ";

    private static final long ONE_HOUR_IN_SECONDS = 60 * 60;
    private static final long FIVE_MINUTES_IN_MILLISECONDS = 1000 * 60 * 5;
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = FIVE_MINUTES_IN_MILLISECONDS * 3;
    private static final long ONE_HOUR_IN_MILLIS = FIFTEEN_MINUTES_IN_MILLISECONDS * 4;

    public CheckProductivityService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CHECK_PRODUCTIVITY)) {
            Log.d(TAG, "Ready!");

            //Ask server for data
            String authToken = Utilities.getAuthToken(this);
            ApiFactory.getApi().getData(authToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response) {
                    if (response.isSuccess()) {
                        try {
                            String html = response.body().string(); //Comes to us as HTML for some reason...

                            //For whatever reason, we get the first part with stuff before a body tag
                            String bodyTag = "<body>";
                            String json = html.substring(html.indexOf(bodyTag) + bodyTag.length()).trim();

                            JSONObject asObj = new JSONObject(json);
                            JSONArray rows = asObj.getJSONArray("rows");

                            long productiveTime = 0;
                            long unproductiveTime = 0;
                            long totalTime = 0;

                            for (int i = 0; i < rows.length(); i++) {
                                JSONArray row = rows.getJSONArray(i);

                                long timeSpent = row.getLong(1);
                                long productivityMeasure = row.getInt(5);

                                if (productivityMeasure > 0) {
                                    productiveTime += timeSpent;
                                } else if (productivityMeasure < 0) {
                                    unproductiveTime += timeSpent;
                                }
                                totalTime += timeSpent;
                            }

                            Log.d(TAG, "Results: " + productiveTime + " " + unproductiveTime + " " + totalTime);
                            float percent = ((float) (unproductiveTime)) / totalTime;
                            boolean inRed = percent > 0.3;

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CheckProductivityService.this);
                            boolean validToNotify = (System.currentTimeMillis() - prefs.getLong(Constants.LAST_NOTIFIED_PREF, -1)) >= ONE_HOUR_IN_MILLIS;
                            Log.d(TAG, "In red: " + inRed);
                            Log.d(TAG, "Valid to notify: " + validToNotify);
                            if (inRed && validToNotify) {
                                DecimalFormat df = new DecimalFormat();
                                df.setMaximumFractionDigits(1);
                                String wastedTimeString = "You've wasted " + df.format(percent * 100) + "% of your time today!";
                                if (totalTime > (3 * ONE_HOUR_IN_SECONDS)) { //Zap
                                    Log.d(TAG, "Sending shock...");
                                    Utilities.sendShock(CheckProductivityService.this, 50);
                                    Utilities.sendNotification(CheckProductivityService.this, "Zap!", wastedTimeString);
                                } else if (totalTime > (2 * ONE_HOUR_IN_SECONDS)) { //Intense buzz
                                    Log.d(TAG, "Sending buzz...");
                                    Utilities.sendVibrate(CheckProductivityService.this, 80, 3, 1000);
                                    Utilities.sendNotification(CheckProductivityService.this, "Buzz!", wastedTimeString);
                                } else if (totalTime > (ONE_HOUR_IN_SECONDS)) { //Gentle buzz
                                    Log.d(TAG, "Sending weak buzz...");
                                    Utilities.sendVibrate(CheckProductivityService.this, 50, 2, 1000);
                                    Utilities.sendNotification(CheckProductivityService.this, "Heads-up...", wastedTimeString);
                                }
                                prefs.edit().putLong(Constants.LAST_NOTIFIED_PREF, System.currentTimeMillis()).apply();
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        Log.d(TAG, "Failure: " + response.code());
                    }
                    stopSelf();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "Failure");
                    stopSelf();
                }
            });

            //Schedule same check for 15 minutes
            Intent alarmIntent = new Intent(this, CheckProductivityService.class);
            alarmIntent.setAction(Constants.ACTION_CHECK_PRODUCTIVITY);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, Constants.ALARM_ID,
                    alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                FIFTEEN_MINUTES_IN_MILLISECONDS - FIVE_MINUTES_IN_MILLISECONDS,
                    SystemClock.elapsedRealtime() + FIFTEEN_MINUTES_IN_MILLISECONDS + FIVE_MINUTES_IN_MILLISECONDS,
                    alarmPendingIntent);
        } else {
            Log.d(TAG, "Not ready!");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
