package com.filipe.footballmatch.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.filipe.footballmatch.Adapters.AvailableEventsAdapter;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by alks_ander on 23/01/2017.
 */

public class ListAvailableEventsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    RecyclerView mRecyclerView;
    AvailableEventsAdapter adapter;
    Context context;

    public static final String TAG = ListAvailableEventsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_list_available_events);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        context = this;

        // An instance of FirebaseAuth is set
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.available_event_recycler_view);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("Event/");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot){

                ArrayList<Event> events = new ArrayList<>();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    events.add(dsp.getValue(Event.class));
                }

                LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);

                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setHasFixedSize(true);

                adapter = new AvailableEventsAdapter(ListAvailableEventsActivity.this, events);
                mRecyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError error){
                // Failed to read value
                Log.w(TAG,"Failed to read value.",error.toException());
            }
        });


    }

}
