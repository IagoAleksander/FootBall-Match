package com.filipe.footballmatch.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ViewProfileActivity extends AppCompatActivity implements UserRepository.OnGetUserImage, UserRepository.OnGetUserInfo{

    @BindView(R.id.profile_picture_iv)
    ImageView profilePicture;

    @BindView(R.id.user_name)
    TextView tvName;

    @BindView(R.id.user_age)
    TextView tvAge;

    @BindView(R.id.user_preferred_position)
    TextView tvPreferredPosition;

    @BindView(R.id.user_contact_number)
    TextView tvContactNumber;

    @BindView(R.id.user_email)
    TextView tvEmail;

    @BindView(R.id.callButton)
    TextView buttonCallPlayer;

    @BindView(R.id.buttonEditProfile)
    TextView buttonEditProfile;

    public static final String TAG = ViewProfileActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    String id;
    Person person;
    Boolean isFromViewMatch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_view_profile);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // A flag is set to check which flow the user is using
        // (If it is from view match or directly from the main menu)
        isFromViewMatch = getIntent().getBooleanExtra("isFromViewMatch", false);

        // The actual user id is recovered
        id = userRepository.getUidCurrentUser();

        // ... and compared with the id that is coming with the intent. If they are equal or if the intent
        //  is not bringing any info about the userKey, it means that the profile that are being shown
        //  is the one of the actual user. It menas that the user can update this profile
        if (getIntent().getStringExtra("userKey") == null
                || getIntent().getStringExtra("userKey").equals(id)) {

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
        } else {
            id = getIntent().getStringExtra("userKey");
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
        tvAge.setText(Integer.toString(person.getAge()));
        tvPreferredPosition.setText(person.getPreferredPosition());

        // If the contact number exists, then the field is populated...
        if (person.getContactNumber() != null
                && !person.getContactNumber().isEmpty()) {
            tvContactNumber.setText(person.getContactNumber());

            // and, if the user is not seeing its own profile, a button call appears,
            // allowing the player to be contacted
            if (buttonEditProfile.getVisibility() == View.GONE) {
                buttonCallPlayer.setVisibility(View.VISIBLE);
                buttonCallPlayer.setOnClickListener(v -> Utility.call(ViewProfileActivity.this, person.getContactNumber()));
            }
        }

        tvEmail.setText(person.getEmail());

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
        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(ViewProfileActivity.this, error);
    }
}
