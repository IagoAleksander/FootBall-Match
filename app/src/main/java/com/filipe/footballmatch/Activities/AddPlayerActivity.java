package com.filipe.footballmatch.Activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.filipe.footballmatch.Repositories.UserRepository;
import com.filipe.footballmatch.Utilities.MessageDialog;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Filipe on 21/01/2017.
 */

public class AddPlayerActivity extends AppCompatActivity implements UserRepository.OnFinished{

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

    public static final String TAG = AddPlayerActivity.class.getSimpleName();
    UserRepository userRepository = new UserRepository();

    Person player = new Person();
    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The content layout of screen is set
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        // The action bar title is customized
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // And finally, the preferred position spinner data is set here
        setSpinner();

        // Click Listener for button confirm, where the data is validated and saved
        buttonConfirm.setOnClickListener(v -> {
            if (validateInfo())
                userRepository.checkIfUserExistsAndSave(player, AddPlayerActivity.this, tilEmail.getEditText().getText().toString().trim());
        });

        // Click Listener for button cancel
        buttonCancel.setOnClickListener(v -> {
            final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, R.string.cancel_profile_update_message, -1, R.string.dialog_edit_no_text, R.string.dialog_edit_yes_text);
            dialog.setCancelable(false);
            dialog.show();
            dialog.noButton.setOnClickListener(view -> dialog.dismiss());
            dialog.yesButton.setOnClickListener(view -> finish());
        });

    }

    // The inserted info is checked and validated
    public boolean validateInfo() {

        // Email data must follow a pattern
        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean validated = true;

        // A new instance of person is populated with the inserted info
        player.setName(tilName.getEditText().getText().toString().trim());
        try {
            player.setAge(Integer.parseInt(tilAge.getEditText().getText().toString().trim()));
            tilAge.setErrorEnabled(false);
        } catch (NumberFormatException e) {
            tilAge.setError(getString(R.string.error_invalid_age));
            validated = false;
        }
        player.setPreferredPosition(spPreferredPosition.getSelectedItem().toString());
        player.setContactNumber(tilContactNumber.getEditText().getText().toString().trim());
        player.setEmail(tilEmail.getEditText().getText().toString().trim());

        if (player.getName().isEmpty()) {
            tilName.setError(getString(R.string.error_invalid_name));
            validated = false;
        } else {
            tilName.setErrorEnabled(false);
        }

        if (!pattern.matcher(player.getEmail()).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            validated = false;
        } else {
            tilEmail.setErrorEnabled(false);
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
    }

    @Override
    public void onUserSaveSuccess() {
        final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, "Player added to the database.", R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> {
            dialog.cancel();
            finish();
        });
    }

    @Override
    public void onUserSaveFailed(String exception) {
        final MessageDialog dialog = new MessageDialog(AddPlayerActivity.this, exception, R.string.dialog_edit_ok_text, -1, -1);
        dialog.setCancelable(false);
        dialog.show();
        dialog.okButton.setOnClickListener(view -> dialog.cancel());
    }
}
