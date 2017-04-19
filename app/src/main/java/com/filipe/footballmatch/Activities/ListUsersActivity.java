package com.filipe.footballmatch.Activities;

import android.content.Context;
import android.content.Intent;
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

import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Adapters.UsersAdapter;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Filipe on 23/01/2017.
 */

public class ListUsersActivity extends AppCompatActivity {


    RecyclerView mRecyclerView;
    UsersAdapter adapter;
    Context context;

    CardView searchUserLayout;
    LinearLayout showFilterLayout;
    CardView addNewPlayerLayout;

    TextInputLayout userNameLayout;
    TextInputLayout userAgeLayout;

    TextView searchUserButton;
    TextView addnewPlayerButton;
    TextView showFilterButton;

    String userName;
    int userAge;

    public boolean isFromCreateMatch = false;

    public static final String TAG = ListUsersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_list_users);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        context = this;

        // A flag is set to check which flow the user is using
        // (If it is from create match or directly from the main menu)
        isFromCreateMatch = getIntent().getBooleanExtra("isFromCreateMatch", false);


        // The layout is now built
        // First, the CardViews that will act as sections for the screen are set
        searchUserLayout = (CardView) findViewById(R.id.search_user_layout);
        addNewPlayerLayout = (CardView) findViewById(R.id.add_new_player_layout);
        showFilterLayout = (LinearLayout) findViewById(R.id.search_user_showFilter);

        // Then, the TextInputLayout fields that will collect the user inputs to filter the list
        userNameLayout = (TextInputLayout) findViewById(R.id.tilName);
        userAgeLayout = (TextInputLayout) findViewById(R.id.tilAge);

        // The TextViews that will act as ListUsersActivity screen buttons are set
        searchUserButton = (TextView) findViewById(R.id.search_button);
        addnewPlayerButton = (TextView) findViewById(R.id.add_new_player_button);
        showFilterButton = (TextView) findViewById(R.id.show_filter_button);

        // And, finally, the RecyclerView, that will display the list of elements
        mRecyclerView = (RecyclerView) findViewById(R.id.users_recycler_view);

        //Click Listener for button search user
        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // The filter section is hided, a show filter button is set and the list is displayed with the results
                searchUserLayout.setVisibility(View.GONE);
                showFilterLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);

                if (!userNameLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userName = userNameLayout.getEditText().getText().toString().trim();
                }
                if (!userAgeLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userAge = Integer.parseInt(userAgeLayout.getEditText().getText().toString().trim());
                }

                // An instance of FirebaseDatabase is set
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Person/");

                // Read from the database
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<Person> users = new ArrayList<>();

                        // Recover all the items from the database that match with the filter
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if ((userNameLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getName().equals(userName))
                                    && (userAgeLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getAge() == userAge)) {

                                users.add(dsp.getValue(Person.class));
                                users.get(users.size() - 1).setUserKey(dsp.getKey());
                            }
                        }

                        // set the RecyclerView parameters
                        LinearLayoutManager llm = new LinearLayoutManager(context);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);

                        mRecyclerView.setLayoutManager(llm);
                        mRecyclerView.setHasFixedSize(true);

                        // Update the list with the results
                        adapter = new UsersAdapter(ListUsersActivity.this, users);
                        mRecyclerView.setAdapter(adapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                        Utility.generalError(ListUsersActivity.this, error.getMessage());
                    }
                });
            }
        });

        //Click Listener for button show filter
        showFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserLayout.setVisibility(View.VISIBLE);
                showFilterLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            }
        });

        // If the user reach this screen form the create match flow, a new option appears
        // this option allows the user to add a new player to the match (not registered in the app)
        if (isFromCreateMatch) {
            addNewPlayerLayout.setVisibility(View.VISIBLE);
            addnewPlayerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ListUsersActivity.this, AddPlayerActivity.class);
                    ListUsersActivity.this.startActivity(intent);
                }
            });
        }

    }

}
