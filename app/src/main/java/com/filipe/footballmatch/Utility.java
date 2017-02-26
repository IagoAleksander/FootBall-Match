package com.filipe.footballmatch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by alks_ander on 25/02/2017.
 */

public class Utility {

    public static boolean isConnectedToNet(Context _context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
