package com.filipe.footballmatch.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;
import com.filipe.footballmatch.Adapters.UsersAdapter;
import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Filipe on 23/01/2017.
 */

public class ListUsersActivity extends AppCompatActivity implements UserRepository.OnGetUsersList{

    UsersAdapter adapter;
    Context context;

    @BindView(R.id.users_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.search_user_layout)
    CardView searchUserLayout;

    @BindView(R.id.search_user_showFilter)
    LinearLayout showFilterLayout;

    @BindView(R.id.add_new_player_layout)
    CardView addNewPlayerLayout;

    @BindView(R.id.tilName)
    TextInputLayout userNameLayout;

    @BindView(R.id.tilAge)
    TextInputLayout userAgeLayout;

    @BindView(R.id.search_button)
    TextView searchUserButton;

    @BindView(R.id.add_new_player_button)
    TextView addnewPlayerButton;

    @BindView(R.id.show_filter_button)
    TextView showFilterButton;

    String userName;
    String userAge;

    public boolean isFromCreateMatch = false;

    public static final String TAG = ListUsersActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_list_users);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        context = this;

        // A flag is set to check which flow the user is using
        // (If it is from create match or directly from the main menu)
        isFromCreateMatch = getIntent().getBooleanExtra("isFromCreateMatch", false);

        //Click Listener for button search user
        searchUserButton.setOnClickListener(v -> {

            // The filter section is hided, a show filter button is set and the list is displayed with the results
            searchUserLayout.setVisibility(View.GONE);
            showFilterLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);

            userName = userNameLayout.getEditText().getText().toString().trim();
            userAge = userAgeLayout.getEditText().getText().toString().trim();

            userRepository.fetchListUsers(this);

            // set the RecyclerView parameters
            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
        });

        //Click Listener for button show filter
        showFilterButton.setOnClickListener(v -> {
            searchUserLayout.setVisibility(View.VISIBLE);
            showFilterLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        });

        // If the user reach this screen form the create match flow, a new option appears
        // this option allows the user to add a new player to the match (not registered in the app)
        if (isFromCreateMatch) {
            addNewPlayerLayout.setVisibility(View.VISIBLE);
            addnewPlayerButton.setOnClickListener(v -> {
                Intent intent = new Intent(ListUsersActivity.this, AddPlayerActivity.class);
                ListUsersActivity.this.startActivity(intent);
            });
        }

    }

    @Override
    public void OnGetUsersListSuccess(DataSnapshot dataSnapshot) {

        ArrayList<Person> users = new ArrayList<>();

        // Recover all the items from the database that match with the filter
        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
            if ((userName.isEmpty() || dsp.getValue(Person.class).getName().equals(userName))
                    && (userAge.isEmpty() || dsp.getValue(Person.class).getAge() == Integer.parseInt(userAge))) {

                users.add(dsp.getValue(Person.class));
                users.get(users.size() - 1).setUserKey(dsp.getKey());
            }
        }

        // set the RecyclerView parameters
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);

        // Update the list with the results
        adapter = new UsersAdapter(ListUsersActivity.this, users);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void OnGetUsersListFailed(String error) {
        // Failed to read value
        Log.e(TAG, error);
        Utility.generalError(ListUsersActivity.this, error);
    }
}
