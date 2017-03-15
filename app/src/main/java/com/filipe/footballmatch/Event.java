package com.filipe.footballmatch;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Filipe on 23/01/2017.
 */

public class Event extends AppCompatActivity {

    private String name;
    private String place;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

}
