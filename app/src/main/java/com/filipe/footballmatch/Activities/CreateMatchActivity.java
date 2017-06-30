package com.filipe.footballmatch.Activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
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

import com.filipe.footballmatch.Repositories.EventRepository;
import com.filipe.footballmatch.Repositories.UserRepository;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.value;
import static com.filipe.footballmatch.R.id.buttonCancel;

public class CreateMatchActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, UserRepository.OnGetUsersList, EventRepository.OnFinished {

    private static final String TAG = "CreateMatchActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ADD_PLAYER_REQUEST_CODE = 1002;

    UserRepository userRepository = new UserRepository();
    EventRepository eventRepository = new EventRepository();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @BindView(R.id.section2)
    LinearLayout section2;

    @BindView(R.id.section3)
    RelativeLayout section3;

    @BindView(R.id.event_players_list)
    LinearLayout playerListLayout;

    @BindView(R.id.venue_name)
    TextView mName;

    @BindView(R.id.venue_address)
    TextView mAddress;

    @BindView(R.id.venue_phone)
    TextView mPhone;

    @BindView(R.id.spinner)
    Spinner mSpinner;

    @BindView(R.id.pickerButton)
    TextView pickerButton;

    @BindView(R.id.event_time)
    TextView mTime;

    @BindView(R.id.event_date)
    TextView mDate;

    @BindView(R.id.date_picker)
    TextView datePicker;

    @BindView(R.id.time_picker)
    TextView timePicker;

    @BindView(R.id.tilEventName)
    TextInputLayout eventNameLayout;

    @BindView(R.id.callButton)
    TextView callButton;

    @BindView(R.id.buttonAddPlayer)
    TextView addPlayerButton;

    @BindView(R.id.buttonCreateEvent)
    TextView createEventButton;

    @BindView(R.id.buttonCancel)
    TextView cancelButton;

    Place place;

    private String eventName;
    private String placeName;
    private Date eventDate;
    private ArrayList<String> playerIdList = new ArrayList<>();
    private ArrayList<Person> playerList = new ArrayList<>();

    private int maxPlayers = 0;
    private String id;
    private boolean pickerClicked = false;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_create_match);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // The spinner that will allow the user to select the number of players in the match is set
        String[] items = new String[]{"10 (5x2)", "14 (7x2)", "22 (11x2)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, items);
        mSpinner.setAdapter(adapter);

        // The actual user id is recovered
        id = userRepository.getUidCurrentUser();

        // Click Listener for button choose match venue
        pickerButton.setOnClickListener(v -> {
            pickerClicked = true;
            getLocation();
        });

        // Click Listener for button call venue
        callButton.setOnClickListener(v -> Utility.call(CreateMatchActivity.this, place.getPhoneNumber().toString()));

        // Click Listener for timePicker, opens time picker fragment
        timePicker.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(fm, "timePicker");
        });

        // Click Listener for datePicker, opens date picker fragment
        datePicker.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(fm, "datePicker");
        });

        // Click Listener for button add player
        addPlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateMatchActivity.this, ListUsersActivity.class);
            intent.putExtra("isFromCreateMatch", true);
            CreateMatchActivity.this.startActivityForResult(intent, ADD_PLAYER_REQUEST_CODE);
        });

        // Click Listener for button create event
        createEventButton.setOnClickListener(v -> {
            if (validateData()) {
                if (Utility.isConnectedToNet(CreateMatchActivity.this)) {
                    registerMatch();
                } else {
                    Utility.noNetworkError(CreateMatchActivity.this);
                }
            } else {
                displayErrorPopup();
            }
        });

        // Click Listener for button cancel
        cancelButton.setOnClickListener(v -> {
            final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.cancel_register_match_message, -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
            dialog.setCancelable(false);
            dialog.show();
            dialog.noButton.setOnClickListener(view -> dialog.dismiss());
            dialog.yesButton.setOnClickListener(view -> finish());
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
            } else {
                mName.setText("");
                mName.setVisibility(View.GONE);
            }

            if (address.length() != 0) {
                mAddress.setText(address);
                section2.setVisibility(View.VISIBLE);
            } else {
                mAddress.setText("");
                section2.setVisibility(View.GONE);
            }

            if (phone.length() != 0) {
                mPhone.setText(phone);
                section3.setVisibility(View.VISIBLE);
            } else {
                mPhone.setText("");
                section3.setVisibility(View.GONE);
            }

            pickerButton.setText(getString(R.string.change_match_venue_button));

        } else if (requestCode == ADD_PLAYER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            String userId = data.getStringExtra("userId");

            switch (mSpinner.getSelectedItemPosition()) {
                case 0:
                    maxPlayers = 10;
                    break;
                case 1:
                    maxPlayers = 14;
                    break;
                case 2:
                    maxPlayers = 22;
                    break;
            }

            if (userId == null || userId.isEmpty()) {
                Utility.generalError(CreateMatchActivity.this, getString(R.string.error_general));
            } else if (playerIdList != null) {
                if (playerIdList.contains(userId)) {
                    Utility.generalError(CreateMatchActivity.this, getString(R.string.error_user_already_added));
                } else if (playerIdList.size() >= maxPlayers) {
                    Utility.generalError(CreateMatchActivity.this, getString(R.string.error_player_max_number_exceeded));
                } else {
                    playerIdList.add(userId);
                    userRepository.fetchListUsers(this);
                }
            } else {
                playerIdList = new ArrayList<>();
                playerIdList.add(userId);
                userRepository.fetchListUsers(this);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Check if the permissions for google places API are granted
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
                break;
        }
    }

    // Standard builder for google places API
    private void getLocation() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                    .build();

            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }

    }

    // When the google Places API is connected, it checks for user permissions and request the actual location of the phone.
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (pickerClicked) {
            pickerClicked = false;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateMatchActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLngBounds LIKELY_PLACE = new LatLngBounds(new LatLng(location.getLatitude() - 0.003, location.getLongitude() - 0.003)
                                        , new LatLng(location.getLatitude() + 0.003, location.getLongitude() + 0.003));

                                try {

                                    PlacePicker.IntentBuilder intentBuilder =
                                            new PlacePicker.IntentBuilder();
                                    intentBuilder.setLatLngBounds(LIKELY_PLACE);
                                    Intent intent = intentBuilder.build(CreateMatchActivity.this);
                                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                                } catch (GooglePlayServicesRepairableException
                                        | GooglePlayServicesNotAvailableException e) {
                                    e.printStackTrace();
                                    Utility.generalError(CreateMatchActivity.this, e.getMessage());
                                }
                            }
                        });
            }
        }
    }

    // On google places API connection fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Utility.generalError(CreateMatchActivity.this, connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // The create event data is validated before submitted to the database
    public boolean validateData() {

        boolean dataValidated = true;

        eventName = eventNameLayout.getEditText().getText().toString().trim();
        placeName = mName.getText().toString().trim();


        if (eventName.isEmpty()
                || placeName.isEmpty()
                || mDate.getText().toString().trim().isEmpty()
                || mTime.getText().toString().trim().isEmpty()) {
            dataValidated = false;
        }

        if (dataValidated) {
            String dtStart = mDate.getText().toString().trim() + "T" + mTime.getText().toString().trim();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
            try {
                Date date = format.parse(dtStart);
                if (date.before(new Date())) {
                    dataValidated = false;
                } else {
                    eventDate = date;
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Utility.generalError(CreateMatchActivity.this, e.getMessage());
            }

        }

        switch (mSpinner.getSelectedItemPosition()) {
            case 0:
                maxPlayers = 10;
                break;
            case 1:
                maxPlayers = 14;
                break;
            case 2:
                maxPlayers = 22;
                break;
        }

        if (playerIdList.size() > maxPlayers) {
            dataValidated = false;
        }


        return dataValidated;
    }

    // If there is any error, the user is informed
    public void displayErrorPopup() {

        if (eventName.isEmpty()) {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_invalid_event_name));
        } else if (placeName.isEmpty()) {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_invalid_venue));
        } else if (mDate.getText().toString().trim().isEmpty()) {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_invalid_date));
        } else if (mTime.getText().toString().trim().isEmpty()) {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_invalid_time));
        } else if (playerIdList.size() > maxPlayers) {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_player_max_number_exceeded));
        } else {
            Utility.generalError(CreateMatchActivity.this, getString(R.string.error_past_date));
        }
    }

    // If the data is ok, the match is registered
    public void registerMatch() {

        // Creating Event object
        Event event = new Event();

        // Adding values
        event.setEventName(eventName);
        event.setName(placeName);
        event.setAddress(place.getAddress().toString());
        if (mPhone.length() != 0) {
            event.setPhone(place.getPhoneNumber().toString());
        }
        event.setNumberOfPlayers(mSpinner.getSelectedItem().toString());
        event.setDate(eventDate);
        event.setPlayersIdList(playerIdList);
        event.setCreator(id);

        eventRepository.saveEvent(event, this);
    }

    // For every player added to the event, the app search for its info in the database
    // and populate the layout with the player name and preferred position
    public void populatePlayerLayout() {
        View.inflate(this, R.layout.item_user_list, playerListLayout);

        int i = playerList.size() - 1;
        View view = playerListLayout.getChildAt(i);
        view.setTag(playerList.get(i).getUserKey());
        TextView playerName = (TextView) view.findViewById(R.id.user_name);
        TextView playerPreferredPosition = (TextView) view.findViewById(R.id.user_preferred_position);
        playerName.setText(playerList.get(i).getName());
        playerPreferredPosition.setText(" (" + playerList.get(i).getPreferredPosition() + ")");


        view.setOnClickListener(v -> {
            Intent intent = new Intent(CreateMatchActivity.this, ViewProfileActivity.class);
            intent.putExtra("userKey", v.getTag().toString());
            CreateMatchActivity.this.startActivity(intent);
        });

    }


    @Override
    public void OnGetUsersListSuccess(DataSnapshot dataSnapshot) {

        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
            if ((dsp.getKey() != null && dsp.getKey().equals(id))
            || (dsp.getValue(Person.class).getOldKey() != null
                    && dsp.getValue(Person.class).getOldKey().equals(id))){

                Person temp = dsp.getValue(Person.class);
                temp.setUserKey(dsp.getKey());
                playerList.add(temp);
                populatePlayerLayout();
                Log.d(TAG, "Value is: " + value);
            }
        }
    }

    @Override
    public void OnGetUsersListFailed(String error) {
        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(CreateMatchActivity.this, error);
    }

    @Override
    public void onEventSaveSuccess() {
        final MessageDialog dialog = new MessageDialog(CreateMatchActivity.this, R.string.success_register_match, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> {
            Intent intent = new Intent(CreateMatchActivity.this, MainMenuActivity.class);
            CreateMatchActivity.this.startActivity(intent);

            dialog.cancel();
        });
    }

    @Override
    public void onEventSaveFailed(String exception) {
        // Failed to read value
        Log.e(TAG, exception);
        Utility.generalError(CreateMatchActivity.this, exception);
    }
}