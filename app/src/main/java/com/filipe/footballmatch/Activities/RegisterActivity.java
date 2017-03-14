package com.filipe.footballmatch.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

import static com.filipe.footballmatch.R.id.tilEmail;

/**
 * Created by alks_ander on 21/01/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout editTextName;
    private TextInputLayout editTextAge;
    private TextInputLayout editTextEmail;
    private TextInputLayout editTextConfirmEmail;
    private TextInputLayout editTextPassword;
    private TextInputLayout editTextConfirmPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    public static final String TAG = RegisterActivity.class.getSimpleName();

    Person person = new Person();
    String oldKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_register);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // An instance of FirebaseAuth is set
        mAuth = FirebaseAuth.getInstance();

        // An instance of FirebaseDatabase is set
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Person");

        // The layout is now built
        // First, the TextViews that will act as LoginActivity screen buttons are set
        TextView buttonRegister = (TextView) findViewById(R.id.buttonSave);

        // Then, the TextInputLayout that will allow the user to insert data are set
        editTextName = (TextInputLayout) findViewById(R.id.tilName);
        editTextAge = (TextInputLayout) findViewById(R.id.tilAge);
        editTextEmail = (TextInputLayout) findViewById(tilEmail);
        editTextConfirmEmail = (TextInputLayout) findViewById(R.id.tilConfirmEmail);
        editTextPassword= (TextInputLayout) findViewById(R.id.tilPassword);
        editTextConfirmPassword= (TextInputLayout) findViewById(R.id.tilConfirmPassword);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // Remove old entry of user (if exists)
                    if (oldKey != null && !oldKey.isEmpty()){
                        myRef.child(oldKey).removeValue();
                    }

                    // Creating new user node, which returns the unique key value
                    // new user node would be /User/$userid/
                    String userId = user.getUid();

                    // Storing values to the database
                    myRef.child(userId).setValue(person);

                    // The userId is then saved to Shared Preferences (locally, in the device)
                    SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = saved_values.edit();
                    editor.putString(getString(R.string.user_id_SharedPref), user.getUid());
                    editor.commit();

                    // A message is then displayed, informing the user that the registration was successful
                    final MessageDialog dialog = new MessageDialog(RegisterActivity.this, "user registered with success", R.string.dialog_edit_ok_text, -1, -1);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RegisterActivity.this, MainMenuActivity.class);
                            RegisterActivity.this.startActivity(intent);
                            dialog.cancel();
                        }
                    });

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Click Listener for button register
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (validateInfo()) {
                    gettingExtraValues();
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean validateInfo() {

        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        String email = editTextEmail.getEditText().getText().toString().trim();
        if (!pattern.matcher(email).matches()) {
            editTextEmail.setError("Not a valid email address!");
            validated = false;
        }
        else {
            editTextEmail.setErrorEnabled(false);
        }

        if (!email.equals(editTextConfirmEmail.getEditText().getText().toString().trim())) {
            editTextConfirmEmail.setError("Email differs from the previous one!");
            validated = false;
        }
        else {
            editTextConfirmEmail.setErrorEnabled(false);
        }

        String password = editTextPassword.getEditText().getText().toString().trim();
        if (password.length() <= 5) {
            editTextPassword.setError("Not a valid password!");
            validated = false;
        }
        else {
            editTextPassword.setErrorEnabled(false);
        }


        if (!password.equals(editTextConfirmPassword.getEditText().getText().toString().trim())) {
            editTextConfirmPassword.setError("Password differs from the previous one!");
            validated = false;
        }
        else {
            editTextConfirmPassword.setErrorEnabled(false);
        }
        return validated;

    }

    public void gettingExtraValues() {

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                person.setEmail(editTextEmail.getEditText().getText().toString().trim());

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.getValue(Person.class).getEmail().equals(person.getEmail())) {
                        person = dsp.getValue(Person.class);
                        oldKey = dsp.getKey();

                        // update values
                        person.setName(editTextName.getEditText().getText().toString().trim());
                        person.setAge(Integer.parseInt(editTextAge.getEditText().getText().toString().trim()));
                    }
                }
                storeValues();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });
    }

    public void storeValues() {

        // Getting values to store
        person.setEmail(editTextEmail.getEditText().getText().toString().trim());
        String password = editTextPassword.getEditText().getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(person.getEmail(), password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            final MessageDialog dialog = new MessageDialog(RegisterActivity.this, task.getException().getMessage(), R.string.dialog_edit_ok_text, -1, -1);
                            dialog.setCancelable(false);
                            dialog.show();
                            dialog.okButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mAuth.signOut();
                                    dialog.cancel();
                                }
                            });
                        }
                    }
                });
    }

}
