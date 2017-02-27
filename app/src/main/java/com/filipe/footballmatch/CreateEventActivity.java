//package com.filipe.footballmatch;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
///**
// * Created by alks_ander on 23/01/2017.
// */
//
//public class CreateEventActivity extends AppCompatActivity {
//
//    private EditText editTextName;
//    private EditText editTextPlace;
//    private Button buttonCreate;
//    private Button buttonCancel;
//
//    private FirebaseAuth mAuth;
//    private FirebaseAuth.AuthStateListener mAuthListener;
//    private String name;
//    private String place;
//
//    public static final String TAG = CreateEventActivity.class.getSimpleName();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        setContentView(R.layout.activity_create_event);
//
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.action_bar);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        buttonCreate = (Button) findViewById(R.id.buttonCreate);
//        buttonCancel = (Button) findViewById(R.id.buttonCancel);
//        editTextName = (EditText) findViewById(R.id.editTextName);
//        editTextPlace = (EditText) findViewById(R.id.editTextPlace);
//
//        //Click Listener for button
//        buttonCreate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // User is signed in
//                Log.d(TAG, "eventCreated:");
//
//                name = editTextName.toString();
////              Write a message to the database
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference myRef = database.getReference("Event/" + mAuth.getCurrentUser().getUid());
//
//                //Creating Person object
//                Event event = new Event();
//
//                //Adding values
//                event.setName(name);
//                event.setPlace(place);
////
//                myRef.setValue(event);
//
//            }
//        });
//
//        buttonCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              CreateEventActivity.this.finish();
//
//            }
//        });
//    }
//
//}
