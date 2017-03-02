package com.filipe.footballmatch;
 
/**
 * Created by Belal on 2/23/2016.
 */
public class Person {
    //name and address string 
    private String name;
    private String address;
    private int age;
 
    public Person() {
      /*Blank default constructor essential for Firebase*/
    }
    //Getters and setters
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}