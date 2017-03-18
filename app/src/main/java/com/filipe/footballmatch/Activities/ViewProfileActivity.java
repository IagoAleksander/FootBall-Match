package com.filipe.footballmatch.Activities;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import static android.R.attr.value;

/**
 * Created by Filipe on 21/01/2017.
 */

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvPreferredPosition;
    private TextView tvContactNumber;
    private TextView tvEmail;

    StorageReference profileImageRef;

    public static final String TAG = ViewProfileActivity.class.getSimpleName();

    String id;
    File localFile = null;
    Person person;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_view_profile);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // The layout is now built
        // First, the TextViews that will show the information to the user
        profilePicture = (ImageView) findViewById(R.id.profile_picture_iv);
        tvName = (TextView) findViewById(R.id.user_name);
        tvAge = (TextView) findViewById(R.id.user_age);
        tvPreferredPosition = (TextView) findViewById(R.id.user_preferred_position);
        tvContactNumber = (TextView) findViewById(R.id.user_contact_number);
        tvEmail = (TextView) findViewById(R.id.user_email);

        // Then, the TextView that will act as ViewProfileActivity screen button is set
        TextView buttonEditProfile = (TextView) findViewById(R.id.buttonEditProfile);

        // The actual user id is recovered from the device local storage...
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        // ... and compared with the id that is coming with the intent. If they are equal or if the intent
        //  is not bringing any info about the userKey, it means that the profile that are being shown
        //  is the one of the actual user. It menas that the user can update this profile
        if (getIntent().getStringExtra("userKey") == null
                || getIntent().getStringExtra("userKey").equals(id)) {

            buttonEditProfile.setVisibility(View.VISIBLE);
        }
        else {
            id = getIntent().getStringExtra("userKey");
        }

        // Now, the app recovers the player image from the cloud storage

        // Get instance of Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://football-match-2c6aa.appspot.com");

        if (!id.isEmpty()) {
            // Create a reference to 'images/id.jpg'
            profileImageRef = storageRef.child("images/" + id + ".jpg");

            // Try create a file with the recovered picture
            try {
                localFile = File.createTempFile(id, "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (localFile != null) {
            profileImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap myImg = BitmapFactory.decodeFile(localFile.getPath());
                    profilePicture.setImageBitmap(myImg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Utility.generalError(ViewProfileActivity.this, exception.getMessage());
                }
            });
        }



        if (!id.isEmpty()) {
            // An instance of FirebaseDatabase is set
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Person/");

            // Read from the database
            myRef.child(id).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    person = dataSnapshot.getValue(Person.class);
                    Log.d(TAG, "Value is: " + value);

                    // The TextViews are, then, populated
                    tvName.setText(person.getName());
                    tvAge.setText(Integer.toString(person.getAge()));
                    tvPreferredPosition.setText(person.getPreferredPosition());
                    tvContactNumber.setText(person.getContactNumber());
                    tvEmail.setText(person.getEmail());

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            // If the chosen profile is the same as of the actual user of the app, it can be edited
            // When the button edit profile is clicked, the data about the user is wrapped and sent
            // to the EditProfileActivity
            buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                    intent.putExtra("person", Parcels.wrap(person));
                    ViewProfileActivity.this.startActivity(intent);
                    finish();
                }
            });

        }

    }


}
