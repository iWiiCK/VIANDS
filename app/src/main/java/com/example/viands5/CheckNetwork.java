package com.example.viands5;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * The prupose of this class is to find out whether the user has an active internet connection when interaction with the application
 * An example would be when the uses searches or scans barcode, refreshes the product details, logging into google accounts etc.
 */
public class CheckNetwork
{
    private final Context context;
    public CheckNetwork(Context context)
    {
        this.context = context;
    }

    //This method returns whether the user has an internet connection
    /////////////////////////////////////////////////////////////////////
    public boolean isOnline()
    {
        boolean isNetworkConnected = false;

        ConnectivityManager connectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] allNetworks = new Network[0];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            allNetworks = connectivityMgr.getAllNetworks();

        for (Network network : allNetworks)
        {
            NetworkCapabilities networkCapabilities = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                networkCapabilities = connectivityMgr.getNetworkCapabilities(network);

            if (networkCapabilities != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        isNetworkConnected = true;
                }
            }
        }

        return isNetworkConnected;
    }
}
