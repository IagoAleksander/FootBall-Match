package com.filipe.footballmatch;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {

    private TextView eventTime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String hourOfDay_text = "";
        String minute_text = "";

        if (hourOfDay < 10) {
            hourOfDay_text = "0" +Integer.toString(hourOfDay);
        }
        else {
            hourOfDay_text = Integer.toString(hourOfDay);
        }

        if (minute < 10) {
            minute_text = "0" +Integer.toString(minute);
        }
        else {
            minute_text = Integer.toString(minute);
        }

        eventTime = (TextView) getActivity().findViewById(R.id.event_time);
        eventTime.setText(hourOfDay_text +":" +minute_text);
    }
}