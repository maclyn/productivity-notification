package com.inipage.productivitypulse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //This is only used by the system, soooooo....
        Intent startService = new Intent(context, CheckProductivityService.class);
        startService.setAction(Constants.ACTION_CHECK_PRODUCTIVITY);
        context.startService(startService);
    }
}
