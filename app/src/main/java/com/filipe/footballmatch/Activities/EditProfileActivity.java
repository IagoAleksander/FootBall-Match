package com.filipe.footballmatch.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.CustomPhotoPickerDialog;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Filipe on 21/01/2017.
 */

public class EditProfileActivity extends AppCompatActivity implements UserRepository.OnFinished, UserRepository.OnGetUserImage {

    @BindView(R.id.profile_picture_iv)
    ImageView profilePicture;

    @BindView(R.id.tilName)
    TextInputLayout tilName;

    @BindView(R.id.tilAge)
    TextInputLayout tilAge;

    @BindView(R.id.spPreferredPosition)
    Spinner spPreferredPosition;

    @BindView(R.id.tilContactNumber)
    TextInputLayout tilContactNumber;

    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;

    @BindView(R.id.buttonConfirm)
    TextView buttonConfirm;

    @BindView(R.id.buttonCancel)
    TextView buttonCancel;

    private static final int SELECT_PICTURE = 1593;
    final static int REQUEST_IMAGE_CAPTURE = 1001;

    public static final String TAG = EditProfileActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    Person person;

    Person editedInfo = new Person();

    private CustomPhotoPickerDialog photoDialog;
    boolean wasPictureChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // Get the person data from the previous activity
        person = Parcels.unwrap(getIntent().getExtras().getParcelable("person"));

        // the email field will not be used in the edit profile flow
        tilEmail.setVisibility(View.GONE);

        // If the person data recovered from the intent is not null, then the fields are populated
        if (person != null) {
            if (person.getName() != null)
                tilName.getEditText().setText(person.getName());
            setSpinner();
            if (person.getContactNumber() != null)
                tilContactNumber.getEditText().setText(person.getContactNumber());

            tilAge.getEditText().setText(Integer.toString(person.getAge()));
        }

        // Now, the app recovers the player image from the cloud storage and the player info from the database
        userRepository.fetchUserImage(userRepository.getUidCurrentUser(), this);

        // Click Listener for button update picture, the user can get an image from its gallery or take a new one using the camera
        profilePicture.setOnClickListener( view-> showOptionsPhoto());

        // Click Listener for button confirm, where the data is validated and saved
        buttonConfirm.setOnClickListener(view-> {

                if (validateInfo()) {
                    Bitmap bitmap = null;

                    if (wasPictureChanged) {
                        profilePicture.setDrawingCacheEnabled(true);
                        profilePicture.buildDrawingCache();
                        bitmap = profilePicture.getDrawingCache();
                    }

                    userRepository.saveUser(editedInfo, bitmap, EditProfileActivity.this);
                }
        });

        // Click Listener for button cancel
        buttonCancel.setOnClickListener(view -> {

                final MessageDialog dialog = new MessageDialog(EditProfileActivity.this, R.string.cancel_profile_update_message, -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
                dialog.setCancelable(false);
                dialog.show();
                dialog.noButton.setOnClickListener(view1 -> dialog.dismiss());
                dialog.yesButton.setOnClickListener(view12 -> {
                    Intent intent = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                    EditProfileActivity.this.startActivity(intent);
                    finish();
                });
        });

    }

    private void showOptionsPhoto() {
        photoDialog = new CustomPhotoPickerDialog(this, new CustomPhotoPickerDialog
                .OnOptionPhotoSelected() {
            @Override
            public void onGallery() {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                photoDialog.dismiss();
            }

            @Override
            public void onCamera() {
                // Here, thisActivity is the current activity
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(EditProfileActivity.this.getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                photoDialog.dismiss();
            }
        });
        photoDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePicture.setImageBitmap(imageBitmap);
            wasPictureChanged = true;
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Glide.with(EditProfileActivity.this).load(imageUri).asBitmap().into(profilePicture);
            wasPictureChanged = true;
        }
    }

    // The inserted info is checked and validated
    public boolean validateInfo() {

        boolean validated = true;

        // A new instance of person is populated with the inserted info
        editedInfo.setName(tilName.getEditText().getText().toString().trim());
        try {
            editedInfo.setAge(Integer.parseInt(tilAge.getEditText().getText().toString().trim()));
            tilAge.setErrorEnabled(false);
        } catch (NumberFormatException e) {
            tilAge.setError(getString(R.string.error_invalid_age));
            validated = false;
        }
        editedInfo.setPreferredPosition(spPreferredPosition.getSelectedItem().toString());
        editedInfo.setContactNumber(tilContactNumber.getEditText().getText().toString().trim());
        editedInfo.setEmail(person.getEmail());

        if (editedInfo.getName() == null || editedInfo.getName().isEmpty()) {
            tilName.setError(getString(R.string.error_invalid_name));
            validated = false;
        } else {
            tilName.setErrorEnabled(false);
        }

        return validated;

    }

    // The preferred position spinner data is set here
    public void setSpinner() {
        String[] positions = new String[]{"Goalkeeper",
                "Right Back",
                "Centre Back",
                "Left Back",
                "Right Wing",
                "Left Wing",
                "Midfielder",
                "Striker"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, positions);
        spPreferredPosition.setAdapter(adapter);

        int position;

        if (person.getPreferredPosition() == null) {
            position = 0;
        } else {
            switch (person.getPreferredPosition()) {
                case "Goalkeeper":
                    position = 0;
                    break;
                case "Right Back":
                    position = 1;
                    break;
                case "Centre Back":
                    position = 2;
                    break;
                case "Left Back":
                    position = 3;
                    break;
                case "Right Wing":
                    position = 4;
                    break;
                case "Left Wing":
                    position = 5;
                    break;
                case "Midfielder":
                    position = 6;
                    break;
                case "Striker":
                    position = 7;
                    break;
                default:
                    position = 0;
                    break;

            }
        }
        spPreferredPosition.setSelection(position);
    }

    @Override
    public void onUserSaveSuccess() {
//        if (mProgressDialog != null) {
//            mProgressDialog.dismiss();
//        }

        final MessageDialog dialog = new MessageDialog(this, R.string.success_update_profile, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> {
            dialog.cancel();
            Intent intent = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
            EditProfileActivity.this.startActivity(intent);
            finish();
        });
    }

    @Override
    public void onUserSaveFailed(String exception) {
//        if (mProgressDialog != null) {
//            mProgressDialog.dismiss();
//        }

        final MessageDialog dialog = new MessageDialog(EditProfileActivity.this, exception, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> dialog.cancel());
    }

    @Override
    public void OnGetUserImageSuccess(Bitmap myImg) {
        profilePicture.setImageBitmap(myImg);
    }
}
