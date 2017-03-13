package com.filipe.footballmatch.Activities;

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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.Manifest;

import com.filipe.footballmatch.Utilities.DatePickerFragment;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.TimePickerFragment;
import com.filipe.footballmatch.Utilities.Utility;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.value;
import static com.filipe.footballmatch.R.id.buttonCancel;

public class CreateMatchActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "CreateMatchActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ADD_PLAYER_REQUEST_CODE = 1002;

    private LinearLayout section2;
    private RelativeLayout section3;
    private LinearLayout playerListLayout;
    private TextView mName;
    private TextView mAddress;
    private TextView mPhone;
    private TextView callButton;
    private Spinner mSpinner;

    private TextView timePicker;
    private TextView mTime;
    private TextView datePicker;
    private TextView mDate;

    private TextView addPlayerButton;
    private TextView createEventButton;
    private TextView cancelButton;

    Place place;
    private String eventName;
    private Date eventDate;
    private ArrayList<String> playerIdList = new ArrayList<>();
    private ArrayList<Person> playerList = new ArrayList<>();

    private int index;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_match);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        section2 = (LinearLayout) findViewById(R.id.section2);
        section3 = (RelativeLayout) findViewById(R.id.section3);
        playerListLayout = (LinearLayout) findViewById(R.id.event_players_list);

        mName = (TextView) findViewById(R.id.venue_name);
        mAddress = (TextView) findViewById(R.id.venue_address);
        mPhone = (TextView) findViewById(R.id.venue_phone);
        callButton = (TextView) findViewById(R.id.callButton);
        TextView pickerButton = (TextView) findViewById(R.id.pickerButton);

        datePicker = (TextView) findViewById(R.id.date_picker);
        mDate = (TextView) findViewById(R.id.event_date);

        timePicker = (TextView) findViewById(R.id.time_picker);
        mTime = (TextView) findViewById(R.id.event_time);

        addPlayerButton = (TextView) findViewById(R.id.buttonAddPlayer);
        createEventButton = (TextView) findViewById(R.id.buttonCreateEvent);
        cancelButton = (TextView) findViewById(buttonCancel);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        String[] items = new String[]{"10 (5x2)", "14 (7x2)", "22 (11x2)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, items);
        mSpinner.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(CreateMatchActivity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();


        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    if (ContextCompat.checkSelfPermission(CreateMatchActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CreateMatchActivity.this,
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
                callVenue(CreateMatchActivity.this, place.getPhoneNumber().toString());
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

        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateMatchActivity.this, ListUsersActivity.class);
                intent.putExtra("isFromCreateMatch", true);
                CreateMatchActivity.this.startActivityForResult(intent, ADD_PLAYER_REQUEST_CODE);
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    if (Utility.isConnectedToNet(CreateMatchActivity.this)) {
                        registerMatch();
                    }
                    else {
                        final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, "cancel?", -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
                dialog.setCancelable(false);
                dialog.show();
                dialog.noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
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
 
        }
        else if (requestCode == ADD_PLAYER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK){
            String userId = data.getStringExtra("userId");

            if (playerIdList.contains(userId)) {
                final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_user_already_added, R.string.dialog_edit_ok_text, -1, -1);
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
                playerIdList.add(userId);
                getIdInfo(userId);
            }
        }
        else {
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

        final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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
                    Intent intent = intentBuilder.build(CreateMatchActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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
                final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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
            final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_invalid_venue, R.string.dialog_edit_ok_text, -1, -1);
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
            final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_invalid_date, R.string.dialog_edit_ok_text, -1, -1);
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
            final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_invalid_time, R.string.dialog_edit_ok_text, -1, -1);
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
            final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.error_past_date, R.string.dialog_edit_ok_text, -1, -1);
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
//        event.setPlace(place);
        event.setName(place.getName().toString());
        event.setAddress(place.getAddress().toString());
        if (mPhone.length() != 0) {
            event.setPhone(place.getPhoneNumber().toString());
        }
        event.setNumberOfPlayers(mSpinner.getSelectedItem().toString());
        event.setDate(eventDate);
        event.setPlayersIdList(playerIdList);
        event.setEventKey(eventId);

        myRef.child(eventId).setValue(event);

        final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.success_register_match, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateMatchActivity.this, MainMenuActivity.class);
                CreateMatchActivity.this.startActivity(intent);

                dialog.cancel();
            }
        });
    }

    public static void callVenue(CreateMatchActivity activity, String number) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        activity.startActivity(intent);

    }

    public void getIdInfo(String id) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Person/");

        // Read from the database
        myRef.child(id).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                playerList.add(dataSnapshot.getValue(Person.class));
                populatePlayerLayout();
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void populatePlayerLayout() {
        View.inflate(this, R.layout.item_user_list, playerListLayout);

        for (int i = 0; i < playerListLayout.getChildCount(); i++) {
            View view = playerListLayout.getChildAt(i);
            TextView playerName = (TextView) view.findViewById(R.id.user_name);
            TextView playerPreferredPosition = (TextView) view.findViewById(R.id.user_preferred_position);
            playerName.setText(playerList.get(i).getName());
            playerPreferredPosition.setText(" (" +playerList.get(i).getPreferredPosition() +")");

            index = i;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreateMatchActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userId",playerList.get(index).getUserKey());
                    CreateMatchActivity.this.startActivity(intent);
                }
            });
        }
    }
}