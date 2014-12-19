package com.binomed.jef.udacityapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sendIntent = new Intent(context, NewsService.class);
        sendIntent.putExtra(NewsService.THEME_QUERY_EXTRA, intent.getStringExtra(NewsService.THEME_QUERY_EXTRA));
        context.startService(sendIntent);
    }
}
