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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.google.android.gms.analytics.internal.zzy.i;

/**
 * Created by alks_ander on 23/01/2017.
 */

public class ListUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    RecyclerView mRecyclerView;
    UsersAdapter adapter;
    Context context;

    TextInputLayout userNameLayout;
    TextInputLayout userAgeLayout;

    Button searchUserButton;

    String userName;
    int userAge;

    public static final String TAG = ListUsersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_users);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        context = this;
        mAuth = FirebaseAuth.getInstance();

        userNameLayout = (TextInputLayout) findViewById(R.id.tilName);
        userAgeLayout = (TextInputLayout) findViewById(R.id.tilAge);
        searchUserButton = (Button) findViewById(R.id.search_button);
        mRecyclerView = (RecyclerView) findViewById(R.id.users_recycler_view);

        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!userNameLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userName = userNameLayout.getEditText().getText().toString().trim();
                }
                if (!userAgeLayout.getEditText().getText().toString().trim().isEmpty()) {
                    userAge = Integer.getInteger(userAgeLayout.getEditText().getText().toString().trim());
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference myRef = database.getReference("Person/");

                // Read from the database
                myRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot){

                        ArrayList<Person> users = new ArrayList<>();

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if ((userNameLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getName().equals(userName))
                                && (userAgeLayout.getEditText().getText().toString().trim().isEmpty()
                                    || dsp.getValue(Person.class).getAge() == userAge))

                                users.add(dsp.getValue(Person.class));
                        }

                        LinearLayoutManager llm = new LinearLayoutManager(context);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);

                        mRecyclerView.setLayoutManager(llm);
                        mRecyclerView.setHasFixedSize(true);

                        adapter = new UsersAdapter(ListUsersActivity.this, users);
                        mRecyclerView.setAdapter(adapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError error){
                        // Failed to read value
                        Log.w(TAG,"Failed to read value.",error.toException());
                    }
                });
            }
        });

    }

}
