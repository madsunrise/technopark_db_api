package com.github.madsunrise.technopark_db_api.builder;

import com.github.madsunrise.technopark_db_api.model.User;

/**
 * Created by ivan on 08.10.16.
 */
public class UserBuilder {
    private final String username;
    private final String name;
    private final String email;
    private final String about;
    private boolean anonymous = false;

    public UserBuilder(String username, String name, String email, String about) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.about = about;
    }

    public UserBuilder anonymous (boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public User build() {
        return new User(username, name, email, about, anonymous);
    }

}
