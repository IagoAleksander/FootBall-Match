package com.filipe.footballmatch.Repositories;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.filipe.footballmatch.Models.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alksander on 6/29/2017.
 */

public class UserRepository {

    private final DatabaseReference reference;
    private FirebaseAuth firebaseAuth;

    private StorageReference storageRef;
    private File localFile = null;

    public UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("Person");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://football-match-2c6aa.appspot.com/");
    }

    public String getUidCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    public void saveUser (final Person user, Bitmap userImage, final OnFinished onFinished) {

        if (userImage != null)
            saveUserImageOnStorage(userImage, user.getUserKey(), new OnImageUpload() {
                @Override
                public void onUploadSuccess(Uri imageUrl) {
//                    user.setPhotoUrl(imageUrl.toString());
                    saveUser(user, onFinished);
                }

                @Override
                public void onUploadFailure(String error) {
                    onFinished.onUserSaveFailed(error);
                }
            });
        else
            saveUser(user, onFinished);
    }

    private void saveUser(final Person user, final OnFinished onFinished) {

        reference.child(getUidCurrentUser()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //callback success
                onFinished.onUserSaveSuccess();

            } else {
                onFinished.onUserSaveFailed(task.getException().getMessage());
            }

        });
    }

    private void saveUserImageOnStorage(Bitmap userImage, String userId, final OnImageUpload onImageUpload) {

        // Create a reference to 'images/id.jpg'
        StorageReference profileImageRef = storageRef.child("images/" + userId + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileImageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            onImageUpload.onUploadFailure(exception.getMessage());
        }).addOnSuccessListener(taskSnapshot -> {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
            onImageUpload.onUploadSuccess(downloadUrl);
        });
    }

    public void checkIfUserExistsAndSave(final Person user, final OnFinished onFinished, final String email) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean userExists = false;

                        // Check the database for the user existence
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if (dsp.getValue(Person.class).getEmail() != null
                                    && dsp.getValue(Person.class).getEmail().equals(email)) {
                                userExists = true;
                            }
                        }

                        if (!userExists) {
                            // If the player did not exists previously and all the info is ok, the new player is added to the database
                            user.setUserKey(reference.push().getKey());
                            saveNewUser(user, onFinished);
                        } else {
                            // The user cannot create a new player if it already exists in the database
                            onFinished.onUserSaveFailed("user already exists");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        onFinished.onUserSaveFailed(databaseError.getMessage());
                    }
                });
    }

    public void getExtraValuesAndSave(final Person player, final OnFinished onFinished) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean userExists = false;
                Person user = player;

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.getValue(Person.class) != null
                            && dsp.getValue(Person.class).getEmail() != null
                            && dsp.getValue(Person.class).getEmail().equals(player.getEmail())) {
                        user = dsp.getValue(Person.class);
                        user.setOldKey(dsp.getKey());
                        user.setName(player.getName());
                        user.setAge(player.getAge());
                        userExists = true;
                    }
                }

                // Remove old entry of user (if exists)
                if (userExists) {
                    reference.child(user.getOldKey()).removeValue();
                }
                user.setUserKey(getUidCurrentUser());
                saveNewUser(user, onFinished);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                onFinished.onUserSaveFailed(error.getMessage());
            }

        });
    }

    private void saveNewUser(final Person user, final OnFinished onFinished) {

        reference.child(user.getUserKey()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //callback success
                onFinished.onUserSaveSuccess();

            } else {
                onFinished.onUserSaveFailed(task.getException().getMessage());
            }

        });
    }

    public void fetchUserImage(final String id, final OnGetUserImage onGetUserImage) {

        // Create a reference to 'images/id.jpg'
        StorageReference profileImageRef = storageRef.child("images/" + id + ".jpg");


        // Try create a file with the recovered picture
        try {
            localFile = File.createTempFile(id, "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localFile != null) {
            profileImageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                onGetUserImage.OnGetUserImageSuccess(BitmapFactory.decodeFile(localFile.getPath()));
            }).addOnFailureListener(exception -> {
                // Handle any errors
            });
        }

    }

    public void fetchUserInfo(final String id, final OnGetUserInfo onGetUserInfo) {
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Person person = dataSnapshot.getValue(Person.class);
                person.setUserKey(dataSnapshot.getKey());
                onGetUserInfo.OnGetUserInfoSuccess(person);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                onGetUserInfo.OnGetUserInfoFailed(error.getMessage());
            }
        });
    }

    public void fetchListUsers(final OnGetUsersList onGetUsersList) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onGetUsersList.OnGetUsersListSuccess(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                onGetUsersList.OnGetUsersListFailed(error.getMessage());
            }
        });
    }

    interface OnImageUpload {
        void onUploadSuccess(Uri imageUrl);

        void onUploadFailure(String error);
    }

    public interface OnFinished {
        void onUserSaveSuccess();

        void onUserSaveFailed(String exception);
    }

    public interface OnGetUserImage {
        void OnGetUserImageSuccess(Bitmap myImg);
    }

    public interface OnGetUserInfo {
        void OnGetUserInfoSuccess(Person user);

        void OnGetUserInfoFailed(String error);
    }

    public interface OnGetUsersList {
        void OnGetUsersListSuccess(DataSnapshot dsp);

        void OnGetUsersListFailed(String error);
    }
}
