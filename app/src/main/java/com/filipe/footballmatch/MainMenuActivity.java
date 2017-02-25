package com.filipe.footballmatch;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.R.attr.value;
import static com.filipe.footballmatch.R.id.buttonLogin;
import static com.filipe.footballmatch.R.id.buttonRegister;

/**
 * Created by alks_ander on 22/01/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

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

        logoutButton = (Button) findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (!id.isEmpty()) {
            DatabaseReference myRef = database.getReference("Person/" + id);

            // Read from the database
            myRef.addValueEventListener(new ValueEventListener() {
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
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                MainMenuActivity.this.startActivity(intent);
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
