package com.filipe.footballmatch;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class ListUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    RecyclerView mRecyclerView;
    UsersAdapter adapter;
    Context context;

    LinearLayout searchUserLayout;
    LinearLayout showFilterLayout;

    TextInputLayout userNameLayout;
    TextInputLayout userAgeLayout;

    TextView searchUserButton;
    TextView showFilterButton;

    String userName;
    int userAge;

    boolean isfromCreateMatch = false;

    public static final String TAG = ListUsersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_users);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        context = this;
        mAuth = FirebaseAuth.getInstance();

        isfromCreateMatch = getIntent().getBooleanExtra("isFromCreateMatch", false);

        searchUserLayout = (LinearLayout) findViewById(R.id.search_user_layout);
        showFilterLayout = (LinearLayout) findViewById(R.id.search_user_showFilter);

        userNameLayout = (TextInputLayout) findViewById(R.id.tilName);
        userAgeLayout = (TextInputLayout) findViewById(R.id.tilAge);
        searchUserButton = (TextView) findViewById(R.id.search_button);
        showFilterButton = (TextView) findViewById(R.id.show_filter_button);
        mRecyclerView = (RecyclerView) findViewById(R.id.users_recycler_view);

        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchUserLayout.setVisibility(View.GONE);
                showFilterLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);

                if (!userNameLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userName = userNameLayout.getEditText().getText().toString().trim();
                }
                if (!userAgeLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userAge = Integer.parseInt(userAgeLayout.getEditText().getText().toString().trim());
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference myRef = database.getReference("Person/");

                // Read from the database
                myRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<Person> users = new ArrayList<>();

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if ((userNameLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getName().equals(userName))
                                    && (userAgeLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getAge() == userAge)) {

                                users.add(dsp.getValue(Person.class));
                                users.get(users.size() - 1).setUserKey(dsp.getKey());
                            }
                        }

                        LinearLayoutManager llm = new LinearLayoutManager(context);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);

                        mRecyclerView.setLayoutManager(llm);
                        mRecyclerView.setHasFixedSize(true);

                        adapter = new UsersAdapter(ListUsersActivity.this, users);
                        mRecyclerView.setAdapter(adapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
            }
        });

        showFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserLayout.setVisibility(View.VISIBLE);
                showFilterLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            }
        });

    }

}
