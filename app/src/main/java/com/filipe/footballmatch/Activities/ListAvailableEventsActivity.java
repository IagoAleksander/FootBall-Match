package com.filipe.footballmatch.Activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Adapters.AvailableEventsAdapter;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.DatePickerFragment;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Filipe on 23/01/2017.
 */

public class ListAvailableEventsActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    AvailableEventsAdapter adapter;

    CardView searchEventLayout;
    LinearLayout showFilterLayout;

    TextInputLayout eventNameLayout;
    TextInputLayout venueNameLayout;
    private TextView mDate;

    TextView searchEventButton;
    TextView showFilterButton;

    String eventName;
    String venueName;

    public static final String TAG = ListAvailableEventsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_list_available_events);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // The layout is now built
        // First, the CardViews that will act as sections for the screen are set
        searchEventLayout = (CardView) findViewById(R.id.search_event_layout);
        showFilterLayout = (LinearLayout) findViewById(R.id.search_event_showFilter);

        // Then, the TextInputLayout fields that will collect the user inputs to filter the list
        eventNameLayout = (TextInputLayout) findViewById(R.id.tilEventName);
        venueNameLayout = (TextInputLayout) findViewById(R.id.tilVenueName);

        // The pickers that will allow the user to choose the date...
        TextView datePicker = (TextView) findViewById(R.id.date_picker);
        mDate = (TextView) findViewById(R.id.event_date);

        // The TextViews that will act as ListUsersActivity screen buttons are set
        searchEventButton = (TextView) findViewById(R.id.search_button);
        showFilterButton = (TextView) findViewById(R.id.show_filter_button);

        // The RecyclerView, that will display the list of elements, is set
        mRecyclerView = (RecyclerView) findViewById(R.id.available_event_recycler_view);

        // when the activity starts, all available events are shown;
        searchDatabase();

        //Click Listener for button search event
        searchEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // The filter section is hided, a show filter button is set and the list is displayed with the results
                searchEventLayout.setVisibility(View.GONE);
                showFilterLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);

                if (!eventNameLayout.getEditText().getText().toString().trim().isEmpty()) {
                    eventName = eventNameLayout.getEditText().getText().toString().trim();
                }

                searchDatabase();
            }
        });

        //Click Listener for button show filter
        showFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEventLayout.setVisibility(View.VISIBLE);
                showFilterLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            }
        });

        // Click Listener for datePicker, opens date picker fragment
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(fm, "datePicker");
            }
        });

    }

    public void searchDatabase() {

        // An instance of FirebaseDatabase is set
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Event/");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Event> events = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

                // Recover all the items from the database that match with the filter
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                    if ((eventNameLayout.getEditText().getText().toString().trim().isEmpty()
                            || dsp.getValue(Event.class).getEventName().equals(eventName))
                            && (venueNameLayout.getEditText().getText().toString().trim().isEmpty()
                            || dsp.getValue(Event.class).getName().contains(venueName))
                            && (mDate.getText().toString().trim().isEmpty()
                            || sdf.format(dsp.getValue(Event.class).getDate()).trim().equals(mDate.getText().toString().trim()))) {
                        events.add(dsp.getValue(Event.class));
                    }
                }

                // set the RecyclerView parameters
                LinearLayoutManager llm = new LinearLayoutManager(ListAvailableEventsActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);

                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setHasFixedSize(true);

                // Update the list with the results
                adapter = new AvailableEventsAdapter(ListAvailableEventsActivity.this, events);
                mRecyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                Utility.generalError(ListAvailableEventsActivity.this, error.getMessage());
            }
        });
    }

}
