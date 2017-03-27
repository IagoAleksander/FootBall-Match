package com.filipe.footballmatch.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.View;

import com.filipe.footballmatch.Activities.CreateMatchActivity;
import com.filipe.footballmatch.Activities.LoginActivity;
import com.filipe.footballmatch.R;

import static android.R.id.message;

/**
 * Created by Filipe on 25/02/2017.
 */

public class Utility {

    public static boolean isConnectedToNet(Context _context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // This method allows the user to call the venue to check for possible times for the event
    public static void call(Activity activity, String number) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        activity.startActivity(intent);

    }

    public static void noNetworkError(Activity activity) {
        final MessageDialog dialog = new MessageDialog(activity, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    public static void generalError (Activity activity, String message) {

        if (message == null) {
            message = activity.getString(R.string.error_general);
        }

        final MessageDialog dialog = new MessageDialog(activity, message, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }
}
