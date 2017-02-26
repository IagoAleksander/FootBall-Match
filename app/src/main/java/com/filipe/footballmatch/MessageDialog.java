package com.filipe.footballmatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MessageDialog {

    public static final String TAG = MessageDialog.class.getSimpleName();

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    Activity activity;

    String dialogLabelText = null;
    SpannableStringBuilder dialogLabelTextSpannable = null;
    String dialogOkBtnText = null;
    String dialogNoBtnText = null;
    String dialogYesBtnText = null;

    TextView dialogLabel;
    public Button okButton;
    public Button noButton;
    public Button yesButton;

    private boolean isSpannable = false;

    public MessageDialog(Activity a, int label, int oklabel, int nolabel, int yeslabel) {
        activity = a;

        if (label != -1)
            dialogLabelText = activity.getResources().getString(label);

        if (oklabel != -1)
            dialogOkBtnText = activity.getResources().getString(oklabel);

        if (nolabel != -1)
            dialogNoBtnText = activity.getResources().getString(nolabel);

        if (yeslabel != -1)
            dialogYesBtnText = activity.getResources().getString(yeslabel);

        createBuilder();
    }

    public MessageDialog(Activity a, String label, int oklabel, int nolabel, int yeslabel) {
        activity = a;

        if ( label != null &&!label.isEmpty())
            dialogLabelText = label;
        else
            dialogLabelText= "error";

        if (oklabel != -1)
            dialogOkBtnText = activity.getResources().getString(oklabel);

        if (nolabel != -1)
            dialogNoBtnText = activity.getResources().getString(nolabel);

        if (yeslabel != -1)
            dialogYesBtnText = activity.getResources().getString(yeslabel);

        createBuilder();
    }

    public void createBuilder(){

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (dialogOkBtnText != null) dialogOkBtnText = dialogOkBtnText.toUpperCase();
            if (dialogNoBtnText != null) dialogNoBtnText = dialogNoBtnText.toUpperCase();
            if (dialogYesBtnText != null) dialogYesBtnText = dialogYesBtnText.toUpperCase();
        }

        builder = new AlertDialog.Builder(activity);

        if(!isSpannable) {
            builder.setMessage(dialogLabelText);
        } else{
            builder.setMessage(dialogLabelTextSpannable);
        }
        builder.setCancelable(false);

        if (dialogOkBtnText != null && dialogYesBtnText == null) {
            builder.setPositiveButton(dialogOkBtnText, null);

        } else if(dialogOkBtnText == null && dialogYesBtnText != null){
            builder.setPositiveButton(dialogYesBtnText, null);

        } else if(dialogOkBtnText != null){
            builder.setPositiveButton(dialogYesBtnText, null);
            builder.setNeutralButton(dialogOkBtnText, null);
        }

        if (dialogYesBtnText != null) {
            builder.setPositiveButton(dialogYesBtnText, null);
        }
        if (dialogNoBtnText != null) {
            builder.setNegativeButton(dialogNoBtnText, null);
        }
        dialog = builder.create();
    }


    public void show() {
        dialog.show();
        noButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        noButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        if (dialogOkBtnText != null && dialogYesBtnText == null) {
            okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            yesButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            okButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            yesButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));

        } else if(dialogOkBtnText == null && dialogYesBtnText != null){
            okButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            yesButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            yesButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));

        } else if(dialogOkBtnText != null){
            okButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            yesButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            yesButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        }

    }

    public void dismiss(){
        dialog.dismiss();
    }

    public void setCancelable(boolean flag){
        dialog.setCancelable(flag);
    }

    public void cancel(){
        dialog.cancel();
    }

    public Window getWindow(){
        return dialog.getWindow();
    }
}
