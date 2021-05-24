package com.example.viands5;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class CheckNetwork
{
    private Context context;


    public CheckNetwork(Context context)
    {
        this.context = context;
    }

    public boolean isOnline()
    {
        boolean isNetworkConnected = false;
        ConnectivityManager connectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] allNetworks = new Network[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            allNetworks = connectivityMgr.getAllNetworks();
        }

        for (Network network : allNetworks) {
            NetworkCapabilities networkCapabilities = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                networkCapabilities = connectivityMgr.getNetworkCapabilities(network);

            if (networkCapabilities != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        isNetworkConnected = true;
                }
            }
        }
        return isNetworkConnected;
    }



    public void checkInternetAvailability()
    {
        boolean isConnected;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (process.waitFor() == 0) isConnected = true;
            else isConnected = false;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
