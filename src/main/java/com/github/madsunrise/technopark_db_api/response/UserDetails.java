package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.User;

/**
 * Created by ivan on 11.10.16.
 */
public class UserDetails {
    protected long id;
    @JsonProperty("name")
    protected String name;
    @JsonProperty("username")
    protected String username;
    @JsonProperty("about")
    protected String about;
    @JsonProperty("email")
    protected String email;
    @JsonProperty("isAnonymous")
    protected boolean anonymous;

    public UserDetails() {
    }

    public UserDetails(long id, String name, String username, String about, String email, boolean anonymous) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.about = about;
        this.email = email;
        this.anonymous = anonymous;
    }

    public UserDetails(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.about = user.getAbout();
        this.email = user.getEmail();
        this.anonymous = user.isAnonymous();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAnonymous() {
        return anonymous;
    }
}

