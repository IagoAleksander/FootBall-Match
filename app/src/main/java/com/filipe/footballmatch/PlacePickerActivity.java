package com.filipe.footballmatch;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.view.View.Z;
import static com.filipe.footballmatch.R.id.callButton;
import static com.filipe.footballmatch.R.id.pickerButton;
import static com.google.android.gms.analytics.internal.zzy.e;
import static com.google.android.gms.analytics.internal.zzy.o;
import static com.google.android.gms.analytics.internal.zzy.p;
import static com.google.android.gms.analytics.internal.zzy.v;

public class PlacePickerActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "PlacePickerActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private LinearLayout section2;
    private RelativeLayout section3;
    private TextView mName;
    private TextView mAddress;
    private TextView mPhone;
    private Button callButton;
    private Spinner mSpinner;

    private Button timePicker;
    private TextView mTime;
    private Button datePicker;
    private TextView mDate;

    private Button createEventButton;

    Place place;
    private String eventName;
    private int eventNumberOfPlayers = 1;
    private Date eventDate;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_place_picker);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        section2 = (LinearLayout) findViewById(R.id.section2);
        section3 = (RelativeLayout) findViewById(R.id.section3);

        mName = (TextView) findViewById(R.id.venue_name);
        mAddress = (TextView) findViewById(R.id.venue_address);
        mPhone = (TextView) findViewById(R.id.venue_phone);
        callButton = (Button) findViewById(R.id.callButton);
        Button pickerButton = (Button) findViewById(R.id.pickerButton);

        datePicker = (Button) findViewById(R.id.date_picker);
        mDate = (TextView) findViewById(R.id.event_date);

        timePicker = (Button) findViewById(R.id.time_picker);
        mTime = (TextView) findViewById(R.id.event_time);

        createEventButton = (Button) findViewById(R.id.buttonCreateEvent);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        Integer[] items = new Integer[]{1, 2, 3, 4};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, items);
        mSpinner.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(PlacePickerActivity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();


        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    if (ContextCompat.checkSelfPermission(PlacePickerActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PlacePickerActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        callPlaceDetectionApi();
                    }

                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callVenue(PlacePickerActivity.this, place.getPhoneNumber().toString());
            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                eventNumberOfPlayers = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(fm, "timePicker");
            }
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(fm, "datePicker");
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    if (Utility.isConnectedToNet(PlacePickerActivity.this)) {
                        registerMatch();
                    }
                    else {
                        final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
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
                else {
                    displayErrorPopup();
                }
            }
        });
    }
 
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
 
        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {
 
            place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            final CharSequence phone = place.getPhoneNumber();

            if (name.length() != 0) {
                mName.setText(name);
                mName.setVisibility(View.VISIBLE);
            }

            if (address.length() != 0) {
                mAddress.setText(address);
                section2.setVisibility(View.VISIBLE);
            }

            if (phone.length() != 0) {
                mPhone.setText(phone);
                section3.setVisibility(View.VISIBLE);
            }
 
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPlaceDetectionApi();
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    private void callPlaceDetectionApi() throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i(TAG, String.format("Place '%s' with " +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }

                LatLngBounds LIKELY_PLACE = new LatLngBounds(new LatLng(likelyPlaces.get(0).getPlace().getLatLng().latitude-0.003, likelyPlaces.get(0).getPlace().getLatLng().longitude-0.003)
                                                , new LatLng(likelyPlaces.get(0).getPlace().getLatLng().latitude+0.003, likelyPlaces.get(0).getPlace().getLatLng().longitude+0.003));

                likelyPlaces.release();

                try {

                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(LIKELY_PLACE);
                    Intent intent = intentBuilder.build(PlacePickerActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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
        });
    }

    public boolean validateData() {

        boolean dataValidated = true;

        eventName = mName.getText().toString().trim();

        if (eventName.isEmpty()) {
            dataValidated = false;
        }
        if (mDate.getText().toString().trim().isEmpty()) {
            dataValidated = false;
        }
        if (mTime.getText().toString().trim().isEmpty()) {
            dataValidated = false;
        }

        if (dataValidated) {
            String dtStart = mDate.getText().toString().trim() + "T" + mTime.getText().toString().trim();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
            try {
                Date date = format.parse(dtStart);
                if (date.before(new Date())) {
                    dataValidated = false;
                }
                else {
                    eventDate = date;
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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

        return dataValidated;
    }

    public void displayErrorPopup() {

        if (eventName.isEmpty()) {
            final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_invalid_venue, R.string.dialog_edit_ok_text, -1, -1);
            dialog.setCancelable(false);
            dialog.show();
            dialog.okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }
        else if (mDate.getText().toString().trim().isEmpty()) {
            final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_invalid_date, R.string.dialog_edit_ok_text, -1, -1);
            dialog.setCancelable(false);
            dialog.show();
            dialog.okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }
        else if (mTime.getText().toString().trim().isEmpty()) {
            final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_invalid_time, R.string.dialog_edit_ok_text, -1, -1);
            dialog.setCancelable(false);
            dialog.show();
            dialog.okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }
        else {
            final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.error_past_date, R.string.dialog_edit_ok_text, -1, -1);
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

    public void registerMatch() {
        //  Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Event");

        // Creating new event node, which returns the unique key value
        // new event node would be /Event/$eventid/
        String eventId = myRef.push().getKey();

        // Creating Event object
        Event event = new Event();

        // Adding values
        event.setPlace(eventName);
        event.setNumberOfPlayers(eventNumberOfPlayers);
        event.setDate(eventDate);

        myRef.child(eventId).setValue(event);

        final MessageDialog dialog = new MessageDialog(PlacePickerActivity.this, R.string.success_register_match, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlacePickerActivity.this, MainMenuActivity.class);
                PlacePickerActivity.this.startActivity(intent);

                dialog.cancel();
            }
        });
    }

    public static void callVenue(PlacePickerActivity activity, String number) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        activity.startActivity(intent);

    }
}