package com.filipe.footballmatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.R.attr.value;
import static android.R.id.message;
import static com.filipe.footballmatch.R.id.buttonLogin;
import static com.filipe.footballmatch.R.id.buttonRegister;

/**
 * Created by alks_ander on 22/01/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    Button searchUserButton;
    Button createMatchButton;
    Button listAvailableMatchesButton;
    Button logoutButton;

    String name;
    int age;

    String id;

    public static final String TAG = MainMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        searchUserButton = (Button) findViewById(R.id.buttonSearchUser);
        createMatchButton = (Button) findViewById(R.id.buttonCreateMatch);
        listAvailableMatchesButton = (Button) findViewById(R.id.buttonListMatches);
        logoutButton = (Button) findViewById(R.id.buttonLogout);

        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ListUsersActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        createMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, PlacePickerActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        listAvailableMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ListAvailableEventsActivity.class);
                MainMenuActivity.this.startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (!id.isEmpty()) {
            DatabaseReference myRef = database.getReference("Person/");

            // Read from the database
            myRef.child(id).addValueEventListener(new ValueEventListener() {
                TextView nameTextView = (TextView) findViewById(R.id.activity_main_menu_name);
                TextView ageTextView = (TextView) findViewById(R.id.activity_main_menu_age);

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Person person = dataSnapshot.getValue(Person.class);
                    name = person.getName();
                    age = person.getAge();
                    Log.d(TAG, "Value is: " + value);
                    nameTextView.setText(name);
                    ageTextView.setText(Integer.toString(age));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }

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

                        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
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
