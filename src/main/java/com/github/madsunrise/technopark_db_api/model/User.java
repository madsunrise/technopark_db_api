package com.github.madsunrise.technopark_db_api.model;


/**
 * Created by ivan on 08.10.16.
 */

public class User {
    private long id;
    private String username;
    private String name;
    private String email;
    private String about;
    private boolean anonymous;

    public User(String username, String name, String email, String about, boolean anonymous) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.about = about;
        this.anonymous = anonymous;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
