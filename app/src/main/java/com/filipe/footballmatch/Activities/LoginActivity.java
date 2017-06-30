package com.filipe.footballmatch.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.support.design.widget.TextInputLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.Login;

/**
 * Created by Filipe on 23/01/2017.
 * This is the login activity. Here, the user can insert the login info,
 * login with google, login with facebook or register a new account
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.tilLogin)
    TextInputLayout login_et;

    @BindView(R.id.tilPassword)
    TextInputLayout password_et;

    @BindView(R.id.buttonLogin)
    TextView buttonLogin;

    @BindView(R.id.google_login)
    TextView buttonGoogleLogin;

    @BindView(R.id.facebook_login)
    TextView buttonFacebookLogin;

    @BindView(R.id.buttonRegister)
    TextView buttonRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    public static final String TAG = LoginActivity.class.getSimpleName();
    private static final int GOOGLE_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // An instance of FirebaseAuth is set
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {

                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                // Get instance of database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("Person/");

                // Check if info about the user already exists in the database
                myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // If there is no info, create a new entry in the database
                        if (!dataSnapshot.exists()) {

                            Log.d(TAG, "New User");

                            // Creating new user node, which returns the unique key value
                            // new user node would be /User/$userid/
                            final String newUserId = user.getUid();

                            // Creating Person object
                            Person person = new Person();

                            // Populating person
                            person.setName(user.getDisplayName());

                            if (FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                                person.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            }

                            // Storing values to the database
                            myRef.child(newUserId).setValue(person);
                        }

                        // And the user is redirected to the MainMenuActivity, now logged in
                        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                        LoginActivity.this.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };

        // Hints are added to the fields to help the user insert the right info
        login_et.setHint(getString(R.string.prompt_login));
        password_et.setHint(getString(R.string.prompt_password));

        // The Listeners for the buttons are now set

        //Click Listener for button register
        buttonRegister.setOnClickListener(v -> {

            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);

        });

        //Click Listener for button login
        buttonLogin.setOnClickListener(v -> {

            if (validateInfo()) {
                if (Utility.isConnectedToNet(LoginActivity.this)) {

                    // Getting login input from the user
                    String email = login_et.getEditText().getText().toString().trim();
                    String password = password_et.getEditText().getText().toString().trim();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        final MessageDialog dialog = new MessageDialog(LoginActivity.this, task.getException().getMessage(), R.string.dialog_edit_ok_text, -1, -1);
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
                } else {
                    Utility.noNetworkError(LoginActivity.this);
                }
            }
        });

        //Click Listener for button google login
        buttonGoogleLogin.setOnClickListener(v -> {

            if (Utility.isConnectedToNet(LoginActivity.this)) {
                signInWithGoogle();
            } else {
                Utility.noNetworkError(LoginActivity.this);
            }
        });

        //Click Listener for button facebook login
        buttonFacebookLogin.setOnClickListener(v -> {
            if (Utility.isConnectedToNet(LoginActivity.this)) {
                signInWithFacebook();
            } else {
                Utility.noNetworkError(LoginActivity.this);
            }
        });

        // Configure Google Sign In
        mGoogleApiClient = configureGoogleSignIn();

        // Configure Facebook Sign In
        callbackManager = configureFacebookSignIn();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onGoogleConnectionFailed:" + connectionResult);
        Utility.generalError(LoginActivity.this, this.getString(R.string.error_google));
        mAuth.signOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //facebook
        if (requestCode == Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    // Create intent for google signIn tentative
    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);

    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Utility.generalError(LoginActivity.this, null);
                        mAuth.signOut();
                    }
                });
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Utility.generalError(LoginActivity.this, null);
                        mAuth.signOut();

                    }

                });
    }

    // The login information provided by the user is validated here
    public boolean validateInfo() {

        // Standard pattern for email
        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        String email = login_et.getEditText().getText().toString().trim();

        // If email is not in agreement with pattern...
        if (!pattern.matcher(email).matches()) {
            login_et.setError(getString(R.string.error_login_invalid_email));
            validated = false;
        } else {
            // Login input is ok, remove error
            login_et.setErrorEnabled(false);
        }

        // If password is not at least 5 character long...
        String password = password_et.getEditText().getText().toString().trim();
        if (password.length() <= 5) {
            password_et.setError(getString(R.string.error_login_invalid_password));
            validated = false;
        } else {
            // Password input is ok, remove error
            password_et.setErrorEnabled(false);
        }

        return validated;

    }

    // Standard Google SignIn configuration
    public GoogleApiClient configureGoogleSignIn() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    // Standard Facebook SignIn configuration
    public CallbackManager configureFacebookSignIn() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onFacebookConnectionFailed:" + error.getMessage());
                Utility.generalError(LoginActivity.this, error.getMessage());
                mAuth.signOut();
            }
        });

        return callbackManager;
    }
}
