package com.binomed.jef.udacityapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.binomed.jef.udacityapp.sync.NewsSyncAdapter;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (isConnected && isWiFi){
            NewsSyncAdapter.syncImmediately(context);
        }

        /*Intent sendIntent = new Intent(context, NewsService.class);
        sendIntent.putExtra(NewsService.THEME_QUERY_EXTRA, intent.getStringExtra(NewsService.THEME_QUERY_EXTRA));
        context.startService(sendIntent);*/
    }
}
