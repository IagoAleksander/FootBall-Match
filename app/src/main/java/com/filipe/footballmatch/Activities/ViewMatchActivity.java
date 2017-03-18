package com.filipe.footballmatch.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.places.Place;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.R.attr.value;
import static com.filipe.footballmatch.R.id.buttonEditProfile;
import static com.filipe.footballmatch.R.id.callButton;
import static com.google.android.gms.analytics.internal.zzy.c;
import static com.google.android.gms.analytics.internal.zzy.i;

/**
 * Created by Filipe on 21/01/2017.
 */

public class ViewMatchActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvAddress;
    private RelativeLayout lPhone;
    private TextView tvPhone;
    private TextView tvNumberOfPlayers;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private LinearLayout lEventPlayers;

    private TextView callButton;
    private TextView buttonEditEvent;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static final String TAG = ViewMatchActivity.class.getSimpleName();

    String eventId;
    Event event;
    ArrayList<String> playerIdList = new ArrayList<>();

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
        tvName = (TextView) findViewById(R.id.venue_name);
        tvAddress = (TextView) findViewById(R.id.venue_address);
        tvNumberOfPlayers = (TextView) findViewById(R.id.event_number_of_players);
        tvPhone = (TextView) findViewById(R.id.venue_phone);
        tvEventDate= (TextView) findViewById(R.id.event_date);
        tvEventTime = (TextView) findViewById(R.id.event_time);

        // Then, the layout that will only appears if the info exists
        lPhone = (RelativeLayout) findViewById(R.id.section3);

        // The list of players in the match will increase dynamically,
        // during app execution
        lEventPlayers = (LinearLayout) findViewById(R.id.event_players_list);

        // Finally, the TextViews that will act as ViewMatchActivity screen buttons are set
        callButton = (TextView) findViewById(R.id.callButton);
        buttonEditEvent = (TextView) findViewById(R.id.buttonEditEvent);

        // An instance of FirebaseDatabase is set
        DatabaseReference myRef = database.getReference("Event/");

        // The eventId is recovered and depends on the item clicked by the user on the ListAvailableEventsActivity
        eventId = getIntent().getStringExtra("eventKey");

        // Read from the database
        myRef.child(eventId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                event = dataSnapshot.getValue(Event.class);
                Log.d(TAG, "Value is: " + value);

                // The TextViews are, then, populated
                tvName.setText(event.getName());
                tvAddress.setText(event.getAddress());

                if (event.getPhone() != null && !event.getPhone().isEmpty()) {
                    lPhone.setVisibility(View.VISIBLE);
                    tvPhone.setText(event.getPhone());

                    callButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callVenue(ViewMatchActivity.this, tvPhone.getText().toString());
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

                playerIdList = event.getPlayersIdList();

                populatePlayerLayout();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

//        buttonEditEvent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ViewMatchActivity.this, EditProfileActivity.class);
//                intent.putExtra("person", Parcels.wrap(event));
//                ViewMatchActivity.this.startActivity(intent);
//                finish();
//            }
//        });


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static void callVenue(ViewMatchActivity activity, String number) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        activity.startActivity(intent);

    }

    // The player layout is then built through information recovered from the database by the playerId
    public void populatePlayerLayout() {

        DatabaseReference myRef = database.getReference("Person/");

        for (String playerId : playerIdList) {

            // Read from the database
            myRef.child(playerId).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    final Person player = dataSnapshot.getValue(Person.class);
                    Log.d(TAG, "Value is: " + value);

                    View view = View.inflate(ViewMatchActivity.this, R.layout.item_user_list, lEventPlayers);
                    TextView playerName = (TextView) view.findViewById(R.id.user_name);
                    TextView playerPreferredPosition = (TextView) view.findViewById(R.id.user_preferred_position);
                    playerName.setText(player.getName());
                    playerPreferredPosition.setText(" (" +player.getPreferredPosition() +")");

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ViewMatchActivity.this, ViewProfileActivity.class);
                            intent.putExtra("userKey", dataSnapshot.getKey());
                            ViewMatchActivity.this.startActivity(intent);
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                    Utility.generalError(ViewMatchActivity.this, error.getMessage());
                }
            });
        }
    }

}
