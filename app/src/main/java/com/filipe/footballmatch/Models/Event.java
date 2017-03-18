package com.filipe.footballmatch.Models;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Filipe on 23/01/2017.
 */

@Parcel
public class Event {

    private String name;
//    private Place place;
    private String address;
    private String phone;
    private Date date;
    private String numberOfPlayers;
    private ArrayList<String> playersIdList;
    private String eventKey;

    public Event() {
      /*Blank default constructor essential for Firebase*/
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(String numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public ArrayList<String> getPlayersIdList() {
        return playersIdList;
    }

    public void setPlayersIdList(ArrayList<String> playersIdList) {
        this.playersIdList = playersIdList;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }
}
