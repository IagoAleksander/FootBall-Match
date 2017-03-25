package com.filipe.footballmatch.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Filipe on 21/01/2017.
 */

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextInputLayout tilName;
    private TextInputLayout tilAge;
    private Spinner spPreferredPosition;
    private TextInputLayout tilContactNumber;
    private TextInputLayout tilEmail;

    private TextView buttonConfirm;
    private TextView buttonCancel;

    StorageReference profileImageRef;

    final static int REQUEST_IMAGE_CAPTURE = 1001;

    public static final String TAG = EditProfileActivity.class.getSimpleName();

    File localFile = null;
    Person person;
    String id;

    boolean wasPictureChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_edit_profile);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // Get the person data from the previous activity
        person = Parcels.unwrap(getIntent().getExtras().getParcelable("person"));

        // The layout is now built
        // First, the TextInputLayouts, the ImageView and the Spinner, that
        // will allow the user to update the profile info are set
        profilePicture = (ImageView) findViewById(R.id.profile_picture_iv);
        tilName = (TextInputLayout) findViewById(R.id.tilName);
        tilAge = (TextInputLayout) findViewById(R.id.tilAge);
        spPreferredPosition = (Spinner) findViewById(R.id.spPreferredPosition);
        tilContactNumber = (TextInputLayout) findViewById(R.id.tilContactNumber);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);

        // Then, the TextViews that will act as LoginActivity screen buttons
        buttonConfirm = (TextView) findViewById(R.id.buttonConfirm);
        buttonCancel = (TextView) findViewById(R.id.buttonCancel);

        // If the person data recovered from the intent is not null, then the fields are populated
        if (person != null) {
            if (person.getName() != null)
                tilName.getEditText().setText(person.getName());
            setSpinner();
            if (person.getContactNumber() != null)
                tilContactNumber.getEditText().setText(person.getContactNumber());

            tilAge.getEditText().setText(Integer.toString(person.getAge()));
        }

        // The actual user id is recovered from the device local storage...
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = saved_values.getString(getString(R.string.user_id_SharedPref), "");

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
                    Utility.generalError(EditProfileActivity.this, exception.getMessage());
                }
            });
        }

        // Click Listener for button update picture
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(EditProfileActivity.this.getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // Click Listener for button confirm
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (wasPictureChanged)
                    addPictureToStorage();
                else
                    updateProfileData();

            }
        });

        // Click Listener for button cancel
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageDialog dialog = new MessageDialog(EditProfileActivity.this, R.string.cancel_profile_update_message, -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
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
                        Intent intent = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                        EditProfileActivity.this.startActivity(intent);
                        finish();
                    }
                });
            }
        });

    }

    // If the picture was updated, the old one is updated by the new one in the storage
    public void addPictureToStorage() {
        // Get the data from an ImageView as bytes
        profilePicture.setDrawingCacheEnabled(true);
        profilePicture.buildDrawingCache();
        Bitmap bitmap = profilePicture.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "error: " + exception.getMessage());
                final MessageDialog dialog = new MessageDialog(EditProfileActivity.this, exception.getMessage(), R.string.dialog_edit_ok_text, -1, -1);
                dialog.setCancelable(false);
                dialog.show();
                dialog.okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "success");
                updateProfileData();
            }
        });
    }

    // If the profile data was changed, the info in the database is updated
    public void updateProfileData() {
        Person editedInfo = new Person();

        editedInfo.setName(tilName.getEditText().getText().toString().trim());
        editedInfo.setAge(Integer.parseInt(tilAge.getEditText().getText().toString().trim()));
        editedInfo.setPreferredPosition(spPreferredPosition.getSelectedItem().toString());
        editedInfo.setContactNumber(tilContactNumber.getEditText().getText().toString().trim());
        editedInfo.setEmail(tilEmail.getEditText().getText().toString().trim());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Person");

        myRef.child(id).setValue(editedInfo);

        final MessageDialog dialog = new MessageDialog(EditProfileActivity.this, R.string.success_update_profile, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                Intent intent = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                EditProfileActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == EditProfileActivity.this.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePicture.setImageBitmap(imageBitmap);
            wasPictureChanged = true;
        }
    }

    // The preferred position spinner data is set here
    public void setSpinner() {
        String[] positions = new String[]{"Goalkeeper",
                "Center-back",
                "Full-back",
                "Wing-back",
                "Holding midfielder",
                "Central",
                "Attacking midfielder",
                "Wide midfielders",
                "Center-forward",
                "Withdrawn striker"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, positions);
        spPreferredPosition.setAdapter(adapter);

        int position;

        if (person.getPreferredPosition() == null) {
            position = 0;
        }
        else {
            switch (person.getPreferredPosition()) {
                case "Goalkeeper":
                    position = 0;
                    break;
                case "Center-back":
                    position = 1;
                    break;
                case "Full-back":
                    position = 2;
                    break;
                case "Wing-back":
                    position = 3;
                    break;
                case "Holding midfielder":
                    position = 4;
                    break;
                case "Central":
                    position = 5;
                    break;
                case "Attacking midfielder":
                    position = 6;
                    break;
                case "Wide midfielders":
                    position = 7;
                    break;
                case "Center-forward":
                    position = 8;
                    break;
                case "Withdrawn striker":
                    position = 9;
                    break;
                default:
                    position = 0;
                    break;

            }
        }
        spPreferredPosition.setSelection(position);
    }

}
