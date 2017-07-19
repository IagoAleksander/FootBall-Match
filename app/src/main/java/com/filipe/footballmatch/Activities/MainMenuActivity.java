package com.filipe.footballmatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Filipe on 22/01/2017.
 * This is the main activity for the user. Here, options are show to
 * the user and each one of them redirected him to a different flow.
 */

public class MainMenuActivity extends AppCompatActivity {

    public static final String TAG = MainMenuActivity.class.getSimpleName();

    @BindView(R.id.buttonViewProfile)
    TextView viewProfileButton;

    @BindView(R.id.buttonSearchUser)
    TextView searchUserButton;

    @BindView(R.id.buttonCreateMatch)
    TextView createMatchButton;

    @BindView(R.id.buttonListMatches)
    TextView listAvailableMatchesButton;

    @BindView(R.id.buttonLogout)
    TextView logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        //Click Listener for button view profile
        viewProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ViewProfileActivity.class);
            MainMenuActivity.this.startActivity(intent);
        });

        //Click Listener for button search user
        searchUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ListUsersActivity.class);
            MainMenuActivity.this.startActivity(intent);
        });

        //Click Listener for button create match
        createMatchButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Can add more as per requirement  

                ActivityCompat.requestPermissions(MainMenuActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 121);
            }
            Intent intent = new Intent(MainMenuActivity.this, CreateMatchActivity.class);
            MainMenuActivity.this.startActivity(intent);
        });

        //Click Listener for button list available matches
        listAvailableMatchesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ListAvailableEventsActivity.class);
            MainMenuActivity.this.startActivity(intent);
        });


        //Click Listener for button logout, here the user is asked to confirm the intention to logout
        logoutButton.setOnClickListener(v -> {
            final MessageDialog dialog = new MessageDialog(MainMenuActivity.this, R.string.logout_confirmation_message,
                    -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
            dialog.setCancelable(false);
            dialog.show();
            dialog.noButton.setOnClickListener(view -> dialog.dismiss());
            dialog.yesButton.setOnClickListener(view -> {
                FirebaseAuth.getInstance().signOut();
                dialog.dismiss();

                // The user is then redirected to the LoginActivity
                Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                MainMenuActivity.this.startActivity(intent);
            });

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
