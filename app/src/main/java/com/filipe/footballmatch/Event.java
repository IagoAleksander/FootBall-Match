package com.filipe.footballmatch;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by alks_ander on 23/01/2017.
 */

public class Event extends AppCompatActivity {

    private String name;
    private String place;
    private Date date;
    private int numberOfPlayers;
    private ArrayList<String> playersIdList;

    public Event() {
      /*Blank default constructor essential for Firebase*/
    }

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public ArrayList<String> getPlayersIdList() {
        return playersIdList;
    }

    public void setPlayersIdList(ArrayList<String> playersIdList) {
        this.playersIdList = playersIdList;
    }
}
