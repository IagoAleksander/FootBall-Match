package com.filipe.footballmatch.Activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Adapters.AvailableEventsAdapter;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Repositories.EventRepository;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.DatePickerFragment;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Filipe on 23/01/2017.
 */

public class ListAvailableEventsActivity extends AppCompatActivity implements EventRepository.OnGetEventsList{

    AvailableEventsAdapter adapter;

    @BindView(R.id.available_event_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.search_event_layout)
    LinearLayout searchEventLayout;

    @BindView(R.id.search_event_showFilter)
    LinearLayout showFilterLayout;

    @BindView(R.id.tilEventName)
    TextInputLayout eventNameLayout;

    @BindView(R.id.tilVenueName)
    TextInputLayout venueNameLayout;

    @BindView(R.id.date_picker)
    TextView datePicker;

    @BindView(R.id.event_date)
    TextView mDate;

    @BindView(R.id.created_by_checkBox)
    CheckBox created_by_checkBox;

    @BindView(R.id.joined_checkBox)
    CheckBox joined_checkBox;

    @BindView(R.id.search_button)
    TextView searchEventButton;

    @BindView(R.id.show_filter_button)
    TextView showFilterButton;

    String eventName;
    String venueName;

    String id;

    public static final String TAG = ListAvailableEventsActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();
    EventRepository eventRepository = new EventRepository();

    public ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_list_available_events);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        progressDialog = ProgressDialog.show(this, null,  "Searching...");

        // The id of the user is recovered
        id = userRepository.getUidCurrentUser();

        // when the activity starts, all available events are shown;
        eventRepository.fetchListEvents(this);

        //Click Listener for button search event
        searchEventButton.setOnClickListener(v -> {

            // The filter section is hided, a show filter button is set and the list is displayed with the results
            searchEventLayout.setVisibility(View.GONE);
            showFilterLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);

            eventName = eventNameLayout.getEditText().getText().toString().trim();
            venueName = venueNameLayout.getEditText().getText().toString().trim();

            eventRepository.fetchListEvents(this);
        });

        //Click Listener for button show filter
        showFilterButton.setOnClickListener(v -> {
            searchEventLayout.setVisibility(View.VISIBLE);
            showFilterLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        });

        // Click Listener for datePicker, opens date picker fragment
        datePicker.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(fm, "datePicker");
        });

    }

    @Override
    public void OnGetEventsListSuccess(DataSnapshot dataSnapshot) {
        ArrayList<Event> events = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        // Recover all the items from the database that match with the filter
        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

            Event temp = dsp.getValue(Event.class);
            if ((eventNameLayout.getEditText().getText().toString().trim().isEmpty()
                    || temp.getEventName().equals(eventName))
                    && (venueNameLayout.getEditText().getText().toString().trim().isEmpty()
                    || temp.getName().contains(venueName))
                    && (mDate.getText().toString().trim().isEmpty()
                    || sdf.format(temp.getDate()).trim().equals(mDate.getText().toString().trim()))
                    && (!created_by_checkBox.isChecked() || temp.getCreator().equals(id))
                    && (!joined_checkBox.isChecked()
                    || (temp.getPlayersIdList() != null && temp.getPlayersIdList().contains(id)))) {
                events.add(temp);
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
    public void OnGetEventsListFailed(String error) {

        progressDialog.dismiss();

        // Failed to read value
        Log.w(TAG, error);
        Utility.generalError(ListAvailableEventsActivity.this, error);
    }
}
