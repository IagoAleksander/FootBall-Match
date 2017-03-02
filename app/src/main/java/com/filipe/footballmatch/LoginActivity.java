package com.filipe.footballmatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.TextInputLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static android.R.attr.id;
import static android.R.attr.value;
import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.Login;
import static com.filipe.footballmatch.R.id.editTextName;
import static com.google.android.gms.internal.zzav.getKey;

public class LoginActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{

    private TextInputLayout login_et;
    private TextInputLayout password_et;
    private TextView buttonRegister;
    private TextView buttonLogin;
    private TextView buttonGoogleLogin;
    private TextView buttonFacebookLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    public static final String TAG = LoginActivity.class.getSimpleName();
    private static final int GOOGLE_SIGN_IN = 9001;

    private  boolean loginWithGoogle = false;
    private  boolean loginWithFacebook = false;
    private boolean userExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" +user.getUid());


                    // Get instance of database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("Person/");

                    // Check if user already exists
                    myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot){

                            // If user still does not exist, create a new entry in the database
                            if (!dataSnapshot.exists()) {

                                Log.d(TAG, "New User");

                                // Creating new user node, which returns the unique key value
                                // new user node would be /User/$userid/
                                final String newUserId = user.getUid();

                                // Creating Person object
                                Person person = new Person();

                                // Adding values
                                person.setName(user.getDisplayName());

                                myRef.child(newUserId).setValue(person);
                            }

                            SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor=saved_values.edit();
                            editor.putString(getString(R.string.user_id_SharedPref), user.getUid());
                            editor.commit();

                            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                            LoginActivity.this.startActivity(intent);

                        }

                        @Override
                        public void onCancelled(DatabaseError error){
                            // Failed to read value
                            Log.w(TAG,"Failed to read value.",error.toException());
                        }
                    });

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        buttonLogin = (TextView) findViewById(R.id.buttonLogin);
        buttonGoogleLogin = (TextView) findViewById(R.id.google_login);
        buttonFacebookLogin = (TextView) findViewById(R.id.facebook_login);
        buttonRegister = (TextView) findViewById(R.id.buttonRegister);
        login_et = (TextInputLayout) findViewById(R.id.tilLogin);
        password_et = (TextInputLayout) findViewById(R.id.tilPassword);

        login_et.setHint(getString(R.string.prompt_login));
        password_et.setHint(getString(R.string.prompt_password));

        //Click Listener for button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateInfo()) {
                    if (Utility.isConnectedToNet(LoginActivity.this)) {

                        // Getting values to store
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
                        final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
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
            }
        });

        buttonGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utility.isConnectedToNet(LoginActivity.this)) {
                    signInWithGoogle();
                }
                else {
                    final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
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
        });

        buttonFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isConnectedToNet(LoginActivity.this)) {
                    signInWithFacebook();
                }
                else {
                    final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_no_network, R.string.dialog_edit_ok_text, -1, -1);
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
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //facebook initialize
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onFacebookConnectionFailed:" + error.getMessage());
                final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onGoogleConnectionFailed:" + connectionResult);
        final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_google, R.string.dialog_edit_ok_text, -1, -1);
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

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //facebook
        if(requestCode == Login.toRequestCode()){
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

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginWithGoogle = true;
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends","email"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginWithFacebook = true;
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            final MessageDialog dialog = new MessageDialog(LoginActivity.this, R.string.error_general, R.string.dialog_edit_ok_text, -1, -1);
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

    public boolean validateInfo() {

        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        String email = login_et.getEditText().getText().toString().trim();
        if (!pattern.matcher(email).matches()) {
            login_et.setError("Please insert a valid email address");
            validated = false;
        }
        else {
            login_et.setErrorEnabled(false);
        }

        String password = password_et.getEditText().getText().toString().trim();
        if (password.length() <= 5) {
            password_et.setError("Please insert a valid password");
            validated = false;
        }
        else {
            password_et.setErrorEnabled(false);
        }

        return validated;

    }
}
