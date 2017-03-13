package com.filipe.footballmatch.Activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by alks_ander on 21/01/2017.
 */

public class AddPlayerActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilAge;
    private Spinner spPreferredPosition;
    private TextInputLayout tilContactNumber;
    private TextInputLayout tilEmail;

    private TextView buttonConfirm;
    private TextView buttonCancel;

    private DatabaseReference myRef;
    ValueEventListener myEventListener;

    public static final String TAG = AddPlayerActivity.class.getSimpleName();

    Person person;
    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Person");

        tilName = (TextInputLayout) findViewById(R.id.tilName);
        tilAge = (TextInputLayout) findViewById(R.id.tilAge);
        spPreferredPosition = (Spinner) findViewById(R.id.spPreferredPosition);
        tilContactNumber = (TextInputLayout) findViewById(R.id.tilContactNumber);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);

        buttonConfirm = (TextView) findViewById(R.id.buttonConfirm);
        buttonCancel = (TextView) findViewById(R.id.buttonCancel);

        setSpinner();

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    checkIfPlayerExists();

            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, R.string.cancel_profile_update_message, -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
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
                        finish();
                    }
                });
            }
        });

    }

    public void checkIfPlayerExists() {

        myEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean userExists = false;
                String email = tilEmail.getEditText().getText().toString().trim();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.getValue(Person.class).getEmail().equals(email)) {
                        userExists = true;
                    }
                }

                if (!userExists) {
                    addNewPlayer();
                } else {
                    final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, "user already exists", R.string.dialog_edit_ok_text, -1, -1);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        };

        // Read from the database
        myRef.addValueEventListener(myEventListener);

    }

    public void addNewPlayer() {

        myRef.removeEventListener(myEventListener);
        Person player = new Person();

        player.setName(tilName.getEditText().getText().toString().trim());
        player.setAge(Integer.parseInt(tilAge.getEditText().getText().toString().trim()));
        player.setPreferredPosition(spPreferredPosition.getSelectedItem().toString());
        player.setContactNumber(tilContactNumber.getEditText().getText().toString().trim());
        player.setEmail(tilEmail.getEditText().getText().toString().trim());

        id = myRef.push().getKey();

        myRef.child(id).setValue(player);

        final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, "Player added to the database.", R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                finish();
            }
        });
    }

    public void setSpinner() {
        String[] positions = new String[]{"Goalkeeper",
                "Center-back",
                "Full-back",
                "Wing-back",
                "Holding midfielder",
                "Central",
                "Attacking midfielder",
                "Wide midfielders",
                "Center-forward",
                "Withdrawn striker"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, positions);
        spPreferredPosition.setAdapter(adapter);
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
