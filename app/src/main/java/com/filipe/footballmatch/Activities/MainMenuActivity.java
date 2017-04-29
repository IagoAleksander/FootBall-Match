package com.filipe.footballmatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.R.attr.value;

/**
 * Created by Filipe on 22/01/2017.
 * This is the main activity for the user. Here, options are show to
 * the user and each one of them redirected him to a different flow.
 */

public class MainMenuActivity extends AppCompatActivity {

    public static final String TAG = MainMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_main_menu);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // The layout is now built
        // First, the TextViews that will act as MainMenuActivity screen buttons are set
        TextView viewProfileButton = (TextView) findViewById(R.id.buttonViewProfile);
        TextView searchUserButton = (TextView) findViewById(R.id.buttonSearchUser);
        TextView createMatchButton = (TextView) findViewById(R.id.buttonCreateMatch);
        TextView listAvailableMatchesButton = (TextView) findViewById(R.id.buttonListMatches);
        TextView logoutButton = (TextView) findViewById(R.id.buttonLogout);

        // The id of the user is recovered from the Shared Preferences
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        // An instance of FirebaseDatabase is set
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // If the id is not empty, data about the user is recovered from the database
        if (!id.isEmpty()) {
            DatabaseReference myRef = database.getReference("Person/");

            // Read from the database
            myRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {

                // The TextView that will show the user greeting in the screen is set
                TextView nameTextView = (TextView) findViewById(R.id.activity_main_menu_name);

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Person person = dataSnapshot.getValue(Person.class);
                    String name = person.getName();
                    Log.d(TAG, "Value is: " + value);

                    // The greeting TextView is populated
                    nameTextView.setText("Hello, " +name +"!");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                    Utility.generalError(MainMenuActivity.this, error.getMessage());
                }
            });
        }

        //Click Listener for button view profile
        viewProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ViewProfileActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        //Click Listener for button search user
        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ListUsersActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        //Click Listener for button create match
        createMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //Can add more as per requirement  

                    ActivityCompat.requestPermissions(MainMenuActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 121);
                }
                Intent intent = new Intent(MainMenuActivity.this, CreateMatchActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        //Click Listener for button list available matches
        listAvailableMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ListAvailableEventsActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });


        //Click Listener for button logout, here the user is asked to confirm the intention to logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageDialog dialog = new MessageDialog(MainMenuActivity.this, R.string.logout_confirmation_message,
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
                        FirebaseAuth.getInstance().signOut();
                        dialog.dismiss();

                        // The user is then redirected to the LoginActivity
                        Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                        MainMenuActivity.this.startActivity(intent);
                    }
                });

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
