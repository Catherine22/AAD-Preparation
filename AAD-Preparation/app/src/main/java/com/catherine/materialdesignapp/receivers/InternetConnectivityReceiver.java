package com.catherine.materialdesignapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import com.catherine.messagecenter.Server;

import static com.catherine.materialdesignapp.utils.OccupiedActions.*;

public class InternetConnectivityReceiver extends BroadcastReceiver {
    public final static String TAG = InternetConnectivityReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Server sv = new Server(context, errorCode -> Log.e(TAG, "onFailure:" + errorCode));
            if (intent.getExtras() != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager == null)
                    return;

                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                if (ni != null && ni.isConnected()) {
                    Bundle b = new Bundle();
                    b.putBoolean(IS_CONNECTED, true);
                    b.putString(TYPE_NAME, ni.getTypeName());
                    sv.pushBundle(ACTION_NETWORK_STATE, b);
                    Log.i(TAG, ni.getTypeName() + " network connected");
                } else {
                    Bundle b = new Bundle();
                    b.putBoolean(IS_CONNECTED, false);
                    sv.pushBundle(ACTION_NETWORK_STATE, b);
                    Log.e(TAG, "Network disabled");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
