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

import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.filipe.footballmatch.R.id.tilAge;
import static com.filipe.footballmatch.R.id.tilEmail;

/**
 * Created by Filipe on 21/01/2017.
 */

public class RegisterActivity extends AppCompatActivity implements UserRepository.OnFinished{

    @BindView(R.id.tilName)
    TextInputLayout editTextName;

    @BindView(R.id.tilAge)
    TextInputLayout editTextAge;

    @BindView(R.id.tilEmail)
    TextInputLayout editTextEmail;

    @BindView(R.id.tilConfirmEmail)
    TextInputLayout editTextConfirmEmail;

    @BindView(R.id.tilPassword)
    TextInputLayout editTextPassword;

    @BindView(R.id.tilConfirmPassword)
    TextInputLayout editTextConfirmPassword;

    @BindView(R.id.buttonSave)
    TextView buttonRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static final String TAG = RegisterActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    Person person = new Person();
    String oldKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // An instance of FirebaseAuth is set
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Registration was successful and user is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                // Storing values to the database
                person.setName(editTextName.getEditText().getText().toString().trim());
                person.setAge(Integer.parseInt(editTextAge.getEditText().getText().toString().trim()));

                userRepository.getExtraValuesAndSave(person, this);
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        };

        // Click Listener for button register
        buttonRegister.setOnClickListener(v -> {
            hideKeyboard();
            if (validateInfo()) {
                if (Utility.isConnectedToNet(RegisterActivity.this)) {
                    storeValues();

                } else {
                    Utility.noNetworkError(RegisterActivity.this);
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

    // The inserted info is checked and validated
    public boolean validateInfo() {

        // Email data must follow a pattern
        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        String name = editTextName.getEditText().getText().toString().trim();
        if (name.isEmpty()) {
            editTextName.setError(getString(R.string.error_invalid_name));
            validated = false;
        } else {
            editTextName.setErrorEnabled(false);
        }

        String email = editTextEmail.getEditText().getText().toString().trim();
        if (!pattern.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.error_invalid_email));
            validated = false;
        } else {
            editTextEmail.setErrorEnabled(false);
        }

        if (!email.equals(editTextConfirmEmail.getEditText().getText().toString().trim())) {
            editTextConfirmEmail.setError(getString(R.string.error_different_email));
            validated = false;
        } else {
            editTextConfirmEmail.setErrorEnabled(false);
        }

        String password = editTextPassword.getEditText().getText().toString().trim();
        if (password.length() <= 5) {
            editTextPassword.setError(getString(R.string.error_invalid_password));
            validated = false;
        } else {
            editTextPassword.setErrorEnabled(false);
        }


        if (!password.equals(editTextConfirmPassword.getEditText().getText().toString().trim())) {
            editTextConfirmPassword.setError(getString(R.string.error_different_password));
            validated = false;
        } else {
            editTextConfirmPassword.setErrorEnabled(false);
        }

        try {
            Integer.parseInt(editTextAge.getEditText().getText().toString().trim());
            editTextAge.setErrorEnabled(false);
        } catch (NumberFormatException e) {
            editTextAge.setError(getString(R.string.error_invalid_age));
            validated = false;
        }

        return validated;

    }

    // If the person was created by another user in the app, when the real user creates an account,
    // this account as extra info coming from the already registered user
    public void getExtraValuesAndSave() {

        // Read from the database


    }

    public void storeValues() {

        // Getting values to store
//        person.setEmail(editTextEmail.getEditText().getText().toString().trim());
        person.setEmail(editTextEmail.getEditText().getText().toString().trim());
        String password = editTextPassword.getEditText().getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(person.getEmail(), password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        try {
                            Utility.generalError(RegisterActivity.this, task.getException().getMessage());
                        } catch (NullPointerException e) {
                            Utility.generalError(RegisterActivity.this, getString(R.string.error_general));
                        }
                        mAuth.signOut();
                    }
                });

    }

    @Override
    public void onUserSaveSuccess() {
        // A message is then displayed, informing the user that the registration was successful
        final MessageDialog dialog = new MessageDialog(RegisterActivity.this, R.string.success_register_user, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, MainMenuActivity.class);
            RegisterActivity.this.startActivity(intent);
            dialog.cancel();
        });
    }

    @Override
    public void onUserSaveFailed(String exception) {
        Log.w(TAG, exception);
        Utility.generalError(RegisterActivity.this, exception);
    }
}
