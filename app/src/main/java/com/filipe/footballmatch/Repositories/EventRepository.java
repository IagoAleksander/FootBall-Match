package com.filipe.footballmatch.Repositories;

import android.support.annotation.NonNull;

import com.filipe.footballmatch.Models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by iago on 30/06/17.
 */

public class EventRepository {

    private final DatabaseReference reference;

    public EventRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("Event");
    }



    public void saveEvent(final Event match, final OnFinished onFinished) {

        if (match.getEventKey() == null) {
            match.setEventKey(reference.push().getKey());
        }

        reference.child(match.getEventKey()).setValue(match).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //callback success
                onFinished.onEventSaveSuccess();
            }
            else {
                onFinished.onEventSaveFailed(task.getException().getMessage());
            }
        });
    }

    public void fetchEventInfo(final String id, final OnGetEventInfo onGetEventInfo) {
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event match = dataSnapshot.getValue(Event.class);
                match.setEventKey(dataSnapshot.getKey());
                onGetEventInfo.OnGetEventInfoSuccess(match);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                onGetEventInfo.OnGetEventInfoFailed(error.getMessage());
            }
        });
    }

    public void fetchListEvents(final OnGetEventsList onGetEventsList) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onGetEventsList.OnGetEventsListSuccess(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                onGetEventsList.OnGetEventsListFailed(error.getMessage());
            }
        });
    }


    public interface OnFinished {
        void onEventSaveSuccess();

        void onEventSaveFailed(String exception);
    }

    public interface OnGetEventsList {
        void OnGetEventsListSuccess(DataSnapshot dsp);

        void OnGetEventsListFailed(String error);
    }

    public interface OnGetEventInfo {
        void OnGetEventInfoSuccess(Event match);

        void OnGetEventInfoFailed(String error);
    }


}
