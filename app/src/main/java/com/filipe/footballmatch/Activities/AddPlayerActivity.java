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
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

/**
 * Created by Filipe on 21/01/2017.
 */

public class AddPlayerActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilAge;
    private Spinner spPreferredPosition;
    private TextInputLayout tilContactNumber;
    private TextInputLayout tilEmail;

    private DatabaseReference myRef;
    ValueEventListener myEventListener;

    public static final String TAG = AddPlayerActivity.class.getSimpleName();

    Person player = new Person();
    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_edit_profile);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // An instance of FirebaseDatabase is set
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Person");

        // The layout is now built
        // First, the TextInputLayouts, the ImageView and the Spinner, that
        // will allow the user to create a new player are set
        tilName = (TextInputLayout) findViewById(R.id.tilName);
        tilAge = (TextInputLayout) findViewById(R.id.tilAge);
        spPreferredPosition = (Spinner) findViewById(R.id.spPreferredPosition);
        tilContactNumber = (TextInputLayout) findViewById(R.id.tilContactNumber);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);

        // Then, the TextViews that will act as LoginActivity screen buttons
        TextView buttonConfirm = (TextView) findViewById(R.id.buttonConfirm);
        TextView buttonCancel = (TextView) findViewById(R.id.buttonCancel);

        // And finally, the preferred position spinner data is set here
        setSpinner();

        // Click Listener for button confirm
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInfo())
                    checkIfPlayerExists();
            }
        });

        // Click Listener for button cancel
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

    // The inserted info is checked and validated
    public boolean validateInfo() {

        // Email data must follow a pattern
        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        // A new instance of person is populated with the inserted info
        player.setName(tilName.getEditText().getText().toString().trim());
        try {
            player.setAge(Integer.parseInt(tilAge.getEditText().getText().toString().trim()));
            tilAge.setErrorEnabled(false);
        } catch (NumberFormatException e) {
            tilAge.setError(getString(R.string.error_invalid_age));
            validated = false;
        }
        player.setPreferredPosition(spPreferredPosition.getSelectedItem().toString());
        player.setContactNumber(tilContactNumber.getEditText().getText().toString().trim());
        player.setEmail(tilEmail.getEditText().getText().toString().trim());

        if (player.getName().isEmpty()) {
            tilName.setError(getString(R.string.error_invalid_name));
            validated = false;
        }
        else {
            tilName.setErrorEnabled(false);
        }

        if (!pattern.matcher(player.getEmail()).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            validated = false;
        }
        else {
            tilEmail.setErrorEnabled(false);
        }

        return validated;

    }

    // The user cannot create a new player if it already exists in the database
    public void checkIfPlayerExists() {

        myEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean userExists = false;
                String email = tilEmail.getEditText().getText().toString().trim();

                // Check the database for the user existence
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.getValue(Person.class).getEmail() != null
                            && dsp.getValue(Person.class).getEmail().equals(email)) {
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
                Utility.generalError(AddPlayerActivity.this, error.getMessage());
            }

        };

        // Read from the database
        myRef.addListenerForSingleValueEvent(myEventListener);

    }

    // If the player did not exists previously and all the info is ok, the new player is added to the database
    public void addNewPlayer() {

        myRef.removeEventListener(myEventListener);

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

    // The preferred position spinner data is set here
    public void setSpinner() {
        String[] positions = new String[]{"Goalkeeper",
                "Right Back",
                "Centre Back",
                "Left Back",
                "Right Wing Midfielder",
                "Left Wing Striker"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, positions);
        spPreferredPosition.setAdapter(adapter);
    }

}
