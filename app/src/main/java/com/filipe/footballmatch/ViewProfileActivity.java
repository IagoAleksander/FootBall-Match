package com.filipe.footballmatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.storage.UploadTask;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.R.attr.id;
import static android.R.attr.value;

/**
 * Created by alks_ander on 21/01/2017.
 */

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvPreferredPosition;
    private TextView tvContactNumber;

    private TextView buttonEditProfile;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    StorageReference profileImageRef;

    final static int REQUEST_IMAGE_CAPTURE = 1001;

    public static final String TAG = ViewProfileActivity.class.getSimpleName();

    String id;
    File localFile = null;
    Person person;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        mAuth = FirebaseAuth.getInstance();

        profilePicture = (ImageView) findViewById(R.id.profile_picture_iv);
        tvName = (TextView) findViewById(R.id.user_name);
        tvAge = (TextView) findViewById(R.id.user_age);
        tvPreferredPosition = (TextView) findViewById(R.id.user_preferred_position);
        tvContactNumber = (TextView) findViewById(R.id.user_contact_number);

        buttonEditProfile = (TextView) findViewById(R.id.buttonEditProfile);

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

        // Get instance of Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://football-match-2c6aa.appspot.com");

        if (!id.isEmpty()) {
            // Create a reference to 'images/id.jpg'
            profileImageRef = storageRef.child("images/" + id + ".jpg");

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
                }
            });
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (!id.isEmpty()) {
            DatabaseReference myRef = database.getReference("Person/");

            // Read from the database
            myRef.child(id).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    person = dataSnapshot.getValue(Person.class);
                    Log.d(TAG, "Value is: " + value);
                    tvName.setText(person.getName());
                    tvAge.setText(Integer.toString(person.getAge()));
                    tvPreferredPosition.setText(person.getPreferredPosition());
                    tvContactNumber.setText(person.getContactNumber());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("person", Parcels.wrap(person));
                ViewProfileActivity.this.startActivity(intent);
            }
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

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
