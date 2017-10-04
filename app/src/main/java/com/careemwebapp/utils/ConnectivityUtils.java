package com.careemwebapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;


/**
 * Created by yuliya on 10/23/15.
 */
public class ConnectivityUtils {

    public interface PingResultListener {
        void onResult(boolean result);
    }

    private ConnectivityInterface connectListener;
    private ConnectivityReceiver connectReceiver;

    public ConnectivityUtils(ConnectivityInterface connectListener) {
        this.connectListener = connectListener;
    }

    public ConnectivityReceiver getConnectReceiver() {
        if (connectReceiver == null) {
            connectReceiver = new ConnectivityReceiver();
        }
        return connectReceiver;
    }

    public static boolean isConnected(Context context) {
        boolean ifConnected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            for (int i = 0; i < info.length; i++) {
                NetworkInfo connection = info[i];
                if (connection.getState().equals(NetworkInfo.State.CONNECTED)) {
                    ifConnected = true;
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return ifConnected;
    }

    public class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (connectListener != null) {
                connectListener.isConnectChange(isConnected(context));
            }
        }
    }

    public interface ConnectivityInterface {
        void isConnectChange(boolean isConnect);
    }

    public static void ping(@NonNull final Context ctx, @NonNull final String url, @NonNull final PingResultListener pingResultListener) {
        if (isConnected(ctx)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL uri = new URL(url);
                        new Socket().connect(new InetSocketAddress(uri.getHost(), uri.getPort()), (int) 1000 * 2);
                        ActivityUtil.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                pingResultListener.onResult(true);
                            }
                        });
                    } catch (Exception ex) {
                        ActivityUtil.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                pingResultListener.onResult(false);
                            }
                        });
                    }
                }
            }).start();
        }
    }
}