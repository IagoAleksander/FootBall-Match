package com.filipe.footballmatch.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.R.attr.value;
import static android.view.View.GONE;
import static com.filipe.footballmatch.R.id.pickerButton;
import static com.filipe.footballmatch.R.id.section2;
import static com.filipe.footballmatch.R.id.section3;
import static com.filipe.footballmatch.Utilities.Utility.call;
import static com.google.android.gms.analytics.internal.zzy.i;

/**
 * Created by Filipe on 21/01/2017.
 */

public class ViewMatchActivity extends AppCompatActivity {

    private static final int REMOVE_PLAYER_REQUEST_CODE = 1001;

    private TextView tvName;
    private TextView tvVenueName;
    private TextView tvAddress;
    private RelativeLayout lPhone;
    private TextView tvPhone;
    private TextView tvNumberOfPlayers;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private LinearLayout lEventPlayers;
    private LinearLayout lSeparator;

    private TextView callButton;
    private TextView buttonEditEvent;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static final String TAG = ViewMatchActivity.class.getSimpleName();

    String id;
    String eventId;
    Event event;
    ArrayList<String> playerIdList = new ArrayList<>();
    private ArrayList<Person> playerList = new ArrayList<>();
    int index;
    boolean isFirstTime = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_view_match);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // The layout is now built
        // First, the TextViews that will show the information to the user
        tvName = (TextView) findViewById(R.id.event_name);
        tvVenueName = (TextView) findViewById(R.id.venue_name);
        tvAddress = (TextView) findViewById(R.id.venue_address);
        tvNumberOfPlayers = (TextView) findViewById(R.id.event_number_of_players);
        tvPhone = (TextView) findViewById(R.id.venue_phone);
        tvEventDate = (TextView) findViewById(R.id.event_date);
        tvEventTime = (TextView) findViewById(R.id.event_time);

        // Then, the layout that will only appears if the info exists
        lPhone = (RelativeLayout) findViewById(section3);
        lSeparator = (LinearLayout) findViewById(R.id.separator_4);

        // The list of players in the match will increase dynamically,
        // during app execution
        lEventPlayers = (LinearLayout) findViewById(R.id.event_players_list);

        // Finally, the TextViews that will act as ViewMatchActivity screen buttons are set
        callButton = (TextView) findViewById(R.id.callButton);
        buttonEditEvent = (TextView) findViewById(R.id.buttonEditEvent);

        // An instance of FirebaseDatabase is set
        DatabaseReference myRef = database.getReference("Event/");

        // The actual user id is recovered from the device local storage...
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        // The eventId is recovered and depends on the item clicked by the user on the ListAvailableEventsActivity
        eventId = getIntent().getStringExtra("eventKey");

        // Read from the database
        myRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                event = dataSnapshot.getValue(Event.class);
                Log.d(TAG, "Value is: " + value);

                // The TextViews are, then, populated
                tvName.setText(event.getEventName());
                tvVenueName.setText(event.getName());
                tvAddress.setText(event.getAddress());

                if (event.getPhone() != null && !event.getPhone().isEmpty()) {
                    lPhone.setVisibility(View.VISIBLE);
                    tvPhone.setText(event.getPhone());

                    callButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utility.call(ViewMatchActivity.this, tvPhone.getText().toString());
                        }
                    });
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
                    lSeparator.setVisibility(View.VISIBLE);
                    playerIdList = event.getPlayersIdList();

                    if (isFirstTime) {
                        isFirstTime = false;
                        for (String id : playerIdList) {
                            getIdInfo(id);
                        }
                    }
                } else {
                    lSeparator.setVisibility(GONE);
                }

                // If the chosen match was created by the actual user of the app, it can be edited
                // When the button edit match is clicked, the data about the match is wrapped and sent
                // to the EditMatchActivity
                if (event.getCreator() != null && event.getCreator().equals(id)) {
                    buttonEditEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ViewMatchActivity.this, EditMatchActivity.class);
                            intent.putExtra("event", Parcels.wrap(event));
                            ViewMatchActivity.this.startActivity(intent);
                        }
                    });
                } else {
//                    buttonEditEvent.setVisibility(GONE);
                    if (!playerIdList.contains(id)) {
                        buttonEditEvent.setText(getString(R.string.join_match_button));
                        buttonEditEvent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                int maxPlayers = 0;
                                switch(event.getNumberOfPlayers()) {
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

                                if (playerIdList.size() > maxPlayers) {
                                    Utility.generalError(ViewMatchActivity.this, getString(R.string.error_join_match));
                                }
                                else {
                                    // add the current user to the match players list
                                    playerIdList.add(id);
                                    getIdInfo(id);

                                    //update the database
                                    updateMatch(getString(R.string.success_join_match));
                                }
                            }
                        });
                    } else {
                        buttonEditEvent.setText(getString(R.string.leave_event_button));
                        buttonEditEvent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final MessageDialog dialog = new MessageDialog(ViewMatchActivity.this, R.string.leave_match_confirmation_message,
                                        -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
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

                                        // remove the current user from the match players id list
                                        removePlayer(id);
                                        dialog.dismiss();

                                    }
                                });
                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    // For every player added to the event, the app search for its info in the database...
    public void getIdInfo(final String id) {

        DatabaseReference myRef = database.getReference("Person/");

        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.getKey() != null
                            && dsp.getKey().equals(id)) {
                        Person temp = dsp.getValue(Person.class);
                        temp.setUserKey(dsp.getKey());
                        playerList.add(temp);
                        populatePlayerLayout();
                        Log.d(TAG, "Value is: " + value);
                    }
                    else if (dsp.getValue(Person.class).getOldKey() != null
                                && dsp.getValue(Person.class).getOldKey().equals(id)) {
                        Person temp = dsp.getValue(Person.class);
                        temp.setUserKey(dsp.getKey());
                        playerList.add(temp);
                        populatePlayerLayout();
                        Log.d(TAG, "Value is: " + value);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                Utility.generalError(ViewMatchActivity.this, error.getMessage());
            }

        });

    }

    // ... and populate the layout with the player name and preferred position
    public void populatePlayerLayout() {

        int i = playerList.size() - 1;
        View.inflate(this, R.layout.item_user_list, lEventPlayers);
        View view = lEventPlayers.getChildAt(i);
        view.setTag(playerList.get(i).getUserKey());
        TextView playerName = (TextView) view.findViewById(R.id.user_name);
        TextView playerPreferredPosition = (TextView) view.findViewById(R.id.user_preferred_position);
        playerName.setText(playerList.get(i).getName());
        playerPreferredPosition.setText(" (" + playerList.get(i).getPreferredPosition() + ")");

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ViewMatchActivity.this, ViewProfileActivity.class);
                intent.putExtra("isFromViewMatch", true);
                intent.putExtra("userKey", v.getTag().toString());
                ViewMatchActivity.this.startActivityForResult(intent, REMOVE_PLAYER_REQUEST_CODE);

            }
        });
    }

    // Update the match players list
    public void updateMatch(String message) {

        //  Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Event");

        // Adding values
        event.setPlayersIdList(playerIdList);

        myRef.child(event.getEventKey()).setValue(event);

        final MessageDialog dialog = new MessageDialog(ViewMatchActivity.this, message, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
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
                    lSeparator.setVisibility(GONE);
                }

                //update the database
                updateMatch(getString(R.string.success_leave_match));
            }

    }

}
