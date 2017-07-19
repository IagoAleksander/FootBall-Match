package com.filipe.footballmatch.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Repositories.EventRepository;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.value;
import static android.view.View.GONE;

/**
 * Created by Filipe on 21/01/2017.
 */

public class ViewMatchActivity extends AppCompatActivity implements EventRepository.OnGetEventInfo, UserRepository.OnGetUsersList, EventRepository.OnFinished {

    private static final int REMOVE_PLAYER_REQUEST_CODE = 1001;

    @BindView(R.id.event_name)
    TextView tvName;

    @BindView(R.id.venue_name)
    TextView tvVenueName;

    @BindView(R.id.venue_address)
    TextView tvAddress;

    @BindView(R.id.section3)
    RelativeLayout lPhone;

    @BindView(R.id.venue_phone)
    TextView tvPhone;

    @BindView(R.id.event_number_of_players)
    TextView tvNumberOfPlayers;

    @BindView(R.id.event_date)
    TextView tvEventDate;

    @BindView(R.id.event_time)
    TextView tvEventTime;

    @BindView(R.id.event_players_list)
    LinearLayout lEventPlayers;

    @BindView(R.id.event_players_layout)
    CardView lEventPlayersLayout;

    @BindView(R.id.callButton)
    TextView callButton;

    @BindView(R.id.buttonEditEvent)
    TextView buttonEditEvent;

    @BindView(R.id.buttonCancelEvent)
    TextView buttonCancelEvent;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    public static final String TAG = ViewMatchActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();
    EventRepository eventRepository = new EventRepository();

    String id;
    String eventId;
    Event event;
    ArrayList<String> playerIdList = new ArrayList<>();
    private ArrayList<Person> playerList = new ArrayList<>();
    boolean isFirstTime = true;

    String message;
    ProgressDialog progressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_view_match);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        progressDialog = ProgressDialog.show(this, null,  "Loading...");

        buttonEditEvent.setVisibility(GONE);

        // An instance of FirebaseDatabase is set
        myRef = database.getReference("Event/");

        // The actual user id is recovered
        id = userRepository.getUidCurrentUser();

        // The eventId is recovered and depends on the item clicked by the user on the ListAvailableEventsActivity
        eventId = getIntent().getStringExtra("eventKey");

        // Read from the database
        eventRepository.fetchEventInfo(eventId, this);

    }

    // The player layout is cleared to be construct again
    public void clearPlayerLayout() {
        View.inflate(this, R.layout.item_user_list, lEventPlayers);
        lEventPlayers.removeAllViews();
    }

    // For every player added to the event, the app search for its info in the database
    // and populate the layout with the player name and preferred position
    public void populatePlayerLayout() {

        int i = playerList.size() - 1;
        View.inflate(this, R.layout.item_user_list, lEventPlayers);
        View view = lEventPlayers.getChildAt(i);
        view.setTag(playerList.get(i).getUserKey());
        TextView playerName = (TextView) view.findViewById(R.id.user_name);
        TextView playerPreferredPosition = (TextView) view.findViewById(R.id.user_preferred_position);
        playerName.setText(playerList.get(i).getName());
        playerPreferredPosition.setText(" (" + playerList.get(i).getPreferredPosition() + ")");

        view.setOnClickListener(v -> {

            Intent intent = new Intent(ViewMatchActivity.this, ViewProfileActivity.class);
            intent.putExtra("isFromViewMatch", true);
            intent.putExtra("userKey", v.getTag().toString());
            ViewMatchActivity.this.startActivityForResult(intent, REMOVE_PLAYER_REQUEST_CODE);

        });
    }

    // Update the match players list
    public void updateMatch(String message) {

        this.message = message;

        // Adding values
        event.setPlayersIdList(playerIdList);

        eventRepository.saveEvent(event, this);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REMOVE_PLAYER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            removePlayer(data.getStringExtra("playerId"));
        }
    }

    public void removePlayer(String id) {

        playerIdList.remove(id);
        for (int i = playerList.size() - 1; i >= 0; i--)
            if (playerList.get(i).getUserKey().equals(id)) {

                // remove the user from the match players list and its layout
                playerList.remove(i);
                View playerView = lEventPlayers.getChildAt(i);
                playerView.setVisibility(GONE);
                lEventPlayers.removeView(playerView);

                if (lEventPlayers.getChildCount() == 0) {
                    lEventPlayersLayout.setVisibility(GONE);
                }

                //update the database
                updateMatch(getString(R.string.success_leave_match));
            }

    }

    public void changeButton(int option) {
        switch (option) {
            // case 0: user is the creator of the event
            case 0:
                buttonEditEvent.setOnClickListener(v -> {
                    if (ActivityCompat.checkSelfPermission(ViewMatchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(ViewMatchActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //Can add more as per requirement  

                        ActivityCompat.requestPermissions(ViewMatchActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 121);
                    } else {
                        Intent intent = new Intent(ViewMatchActivity.this, EditMatchActivity.class);
                        intent.putExtra("event", Parcels.wrap(event));
                        ViewMatchActivity.this.startActivity(intent);
                    }
                });
                buttonCancelEvent.setVisibility(View.VISIBLE);
                buttonCancelEvent.setOnClickListener(v -> {
                    final MessageDialog dialog = new MessageDialog(ViewMatchActivity.this, R.string.cancel_event_confirmation_message,
                            -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.noButton.setOnClickListener(view -> dialog.dismiss());
                    dialog.yesButton.setOnClickListener(view -> {

                        // remove the current event from the database
                        myRef.child(eventId).removeValue();

                        dialog.dismiss();

                        final MessageDialog dialog1 = new MessageDialog(ViewMatchActivity.this, R.string.success_cancel_match, R.string.dialog_edit_ok_text, -1, -1);
                        dialog1.setCancelable(false);
                        dialog1.show();
                        dialog1.okButton.setOnClickListener(view1 -> {
                            Intent intent = new Intent(ViewMatchActivity.this, MainMenuActivity.class);
                            ViewMatchActivity.this.startActivity(intent);

                            dialog1.cancel();
                        });
                    });
                });
                break;
            // case 1: player is not registered in the event
            case 1:
                buttonEditEvent.setText(getString(R.string.join_match_button));
                buttonEditEvent.setOnClickListener(v -> {

                    int maxPlayers = 0;
                    switch (event.getNumberOfPlayers()) {
                        case "10 (5x2)":
                            maxPlayers = 10;
                            break;
                        case "14 (7x2)":
                            maxPlayers = 14;
                            break;
                        case "22 (11x2)":
                            maxPlayers = 22;
                            break;
                    }

                    if (playerIdList.size() >= maxPlayers) {
                        Utility.generalError(ViewMatchActivity.this, getString(R.string.error_join_match));
                    } else {
                        // add the current user to the match players list
                        playerIdList.add(id);

                        //update the database
                        updateMatch(getString(R.string.success_join_match));
                        changeButton(2);
                    }

                });
                break;
            // case 2: player is already registered in the event
            case 2:
                buttonEditEvent.setText(getString(R.string.leave_event_button));
                buttonEditEvent.setOnClickListener(v -> {
                    final MessageDialog dialog = new MessageDialog(ViewMatchActivity.this, R.string.leave_match_confirmation_message,
                            -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.noButton.setOnClickListener(view -> dialog.dismiss());
                    dialog.yesButton.setOnClickListener(view -> {

                        // remove the current user from the match players id list
                        removePlayer(id);
                        dialog.dismiss();
                        changeButton(1);

                    });
                });
                break;
        }

        buttonEditEvent.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnGetEventInfoSuccess(Event match) {

        event = match;

        // The TextViews are, then, populated
        tvName.setText(event.getEventName());
        tvVenueName.setText(event.getName());
        tvAddress.setText(event.getAddress());

        if (event.getPhone() != null && !event.getPhone().isEmpty()) {
            lPhone.setVisibility(View.VISIBLE);
            tvPhone.setText(event.getPhone());

            callButton.setOnClickListener(v -> Utility.call(ViewMatchActivity.this, tvPhone.getText().toString()));
        }

        tvNumberOfPlayers.setText(event.getNumberOfPlayers());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(event.getDate());
        tvEventDate.setText(formattedDate);

        sdf = new SimpleDateFormat("hh:mm a");
        String formattedTime = sdf.format(event.getDate());

        tvEventTime.setText(formattedTime);

        if (event.getPlayersIdList() != null
                && !event.getPlayersIdList().isEmpty()) {
            lEventPlayersLayout.setVisibility(View.VISIBLE);
            playerIdList = event.getPlayersIdList();

            if (isFirstTime) {
                isFirstTime = false;
                userRepository.fetchListUsers(this);
            }
        } else {
            lEventPlayersLayout.setVisibility(GONE);
        }

        // If the chosen match was created by the actual user of the app, it can be edited
        // When the button edit match is clicked, the data about the match is wrapped and sent
        // to the EditMatchActivity
        if (event.getCreator() != null && event.getCreator().equals(id)) {
            changeButton(0);
        } else {
            if (!playerIdList.contains(id)) {
                changeButton(1);
            } else {
                changeButton(2);
            }

        }

        progressDialog.dismiss();
    }

    @Override
    public void OnGetEventInfoFailed(String error) {

        progressDialog.dismiss();

        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(ViewMatchActivity.this, error);
    }

    @Override
    public void OnGetUsersListSuccess(DataSnapshot dataSnapshot) {

        progressDialog.dismiss();

        playerList.clear();
        clearPlayerLayout();

        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
            if (dsp.getKey() != null
                    && playerIdList.contains(dsp.getKey())) {
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

        progressDialog.dismiss();

        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(ViewMatchActivity.this, error);
    }

    @Override
    public void onEventSaveSuccess() {

        progressDialog.dismiss();

        final MessageDialog dialog = new MessageDialog(ViewMatchActivity.this, message, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> {
            eventRepository.fetchEventInfo(eventId, this);
            userRepository.fetchListUsers(this);
            dialog.dismiss();
        });
    }

    @Override
    public void onEventSaveFailed(String exception) {

        progressDialog.dismiss();
        // Failed to read value
        Log.e(TAG, exception);
        Utility.generalError(ViewMatchActivity.this, exception);
    }
}
