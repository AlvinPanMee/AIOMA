package com.bignerdranch.android.aioma;

public class User {

    public String username;
    public String email;
    public boolean customer;

    public User(){

    }

    public User(String username, String email, boolean customer){
        this.username = username;
        this.email = email;
        this.customer = customer;
    }


}
