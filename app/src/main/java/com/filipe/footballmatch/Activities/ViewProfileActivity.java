package com.filipe.footballmatch.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.value;

/**
 * Created by Filipe on 21/01/2017.
 */

public class ViewProfileActivity extends AppCompatActivity implements UserRepository.OnGetUserImage, UserRepository.OnGetUserInfo {

    @BindView(R.id.profile_picture_iv)
    ImageView profilePicture;

    @BindView(R.id.user_name)
    TextView tvName;

    @BindView(R.id.user_preferred_position_age)
    TextView tvPreferredPositionAge;

    @BindView(R.id.contact_number_layout)
    CardView llContactNumber;

    @BindView(R.id.user_contact_number)
    TextView tvContactNumber;

    @BindView(R.id.email_layout)
    CardView llEmail;

    @BindView(R.id.user_email)
    TextView tvEmail;

    @BindView(R.id.callButton)
    FloatingActionButton buttonCallPlayer;

    @BindView(R.id.emailButton)
    FloatingActionButton buttonEmailPlayer;

    @BindView(R.id.buttonEditProfile)
    TextView buttonEditProfile;

    @BindView(R.id.buttonLeaveMatch)
    TextView buttonLeaveMatch;

    public static final String TAG = ViewProfileActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    String id;
    Person person;
    Boolean isFromViewMatch;

    ProgressDialog progressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_view_profile);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        progressDialog = ProgressDialog.show(this, null,  "Loading...");

        // A flag is set to check which flow the user is using
        // (If it is from view match or directly from the main menu)
        isFromViewMatch = getIntent().getBooleanExtra("isFromViewMatch", false);

        // The player id is recovered
        if (getIntent().getStringExtra("userKey") != null) {
            id = getIntent().getStringExtra("userKey");
        } else {
            id = userRepository.getUidCurrentUser();
        }

        // Now, the app recovers the player image from the cloud storage and the player info from the database
        if (!id.isEmpty()) {
            userRepository.fetchUserImage(id, this);
            userRepository.fetchUserInfo(id, this);
        }
    }

    public void populateFields() {
        // The TextViews are, then, populated
        tvName.setText(person.getName());
        tvPreferredPositionAge.setText(person.getPreferredPosition() +", "+person.getAge());

        // If the contact number exists, then the field is populated...
        if (person.getContactNumber() != null
                && !person.getContactNumber().isEmpty()) {
            tvContactNumber.setText(person.getContactNumber());

            // and, if the user is not seeing its own profile, a button call appears,
            // allowing the player to be contacted
            if (llContactNumber.getVisibility() == View.GONE) {
                llContactNumber.setVisibility(View.VISIBLE);
                buttonCallPlayer.setOnClickListener(v -> Utility.call(ViewProfileActivity.this, person.getContactNumber()));
            }
        }
        else {
            llContactNumber.setVisibility(View.GONE);
        }

        if (person.getEmail() != null) {
            tvEmail.setText(person.getEmail());
            llEmail.setVisibility(View.VISIBLE);

            buttonEmailPlayer.setOnClickListener(v -> {
                emailPlayer(person.getEmail());
            });
        }

        // the id is compared with the id that is coming with the intent. If they are equal or if the intent
        //  is not bringing any info about the userKey, it means that the profile that are being shown
        //  is the one of the actual user. It menas that the user can update this profile
        if (id.equals(userRepository.getUidCurrentUser())) {

            buttonEditProfile.setVisibility(View.VISIBLE);

            // If the chosen profile is the same as of the actual user of the app, it can be edited
            // When the button edit profile is clicked, the data about the user is wrapped and sent
            // to the EditProfileActivity
            buttonEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("person", Parcels.wrap(person));
                ViewProfileActivity.this.startActivity(intent);
                finish();
            });

            if (isFromViewMatch) {
                buttonLeaveMatch.setVisibility(View.VISIBLE);

                // If the chosen profile is the same as of the actual user of the app, the user can leave the match
                buttonLeaveMatch.setOnClickListener(v -> {
                    Intent returnIntent = new Intent(ViewProfileActivity.this, ViewMatchActivity.class);
                    returnIntent.putExtra("playerId", person.getUserKey());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            }

        }

        // If the user reach this screen from the view match flow, a new option appears
        // this option allows the user to remove the player from the match
        if (isFromViewMatch && buttonEditProfile.getVisibility() == View.GONE) {
            buttonEditProfile.setText(getString(R.string.remove_player_button));
            buttonEditProfile.setVisibility(View.VISIBLE);
            buttonEditProfile.setOnClickListener(v -> {
                Intent returnIntent = new Intent(ViewProfileActivity.this, ViewMatchActivity.class);
                returnIntent.putExtra("playerId", person.getUserKey());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            });
        }

        progressDialog.dismiss();


    }


    @Override
    public void OnGetUserImageSuccess(Bitmap myImg) {
        profilePicture.setImageBitmap(myImg);
    }

    @Override
    public void OnGetUserInfoSuccess(Person user) {
        person = user;

        populateFields();
    }

    @Override
    public void OnGetUserInfoFailed(String error) {

        progressDialog.dismiss();

        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(ViewProfileActivity.this, error);
    }

    public void emailPlayer(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
        intent.setType("text/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject of email");
        intent.putExtra(Intent.EXTRA_TEXT, "Body of email");
        intent.setData(Uri.parse("mailto:" +email)); // or just "mailto:" for blank
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }
}
