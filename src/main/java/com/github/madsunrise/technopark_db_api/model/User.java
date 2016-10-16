package com.github.madsunrise.technopark_db_api.model;


import com.github.madsunrise.technopark_db_api.DAO.UserDAOImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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
    private Set<String> followers = new HashSet<>();
    private Set<String> following = new HashSet<>();
    private Set<Long> subscriptions = new HashSet<>();

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);

    public User(String username, String name, String email, String about, boolean anonymous) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.about = about;
        this.anonymous = anonymous;
        this.id = ID_GENETATOR.getAndIncrement();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Set<String> getFollowers() {;
        return followers;
    }

    public Set<String> getFollowees() {
        return following;
    }




    public Set<Long> getSubscriptions() {
        return subscriptions;
    }

    public void subscribe (Long threadId) {
        subscriptions.add(threadId);
    }

    public void unsubscribe (Long threadId) {
        subscriptions.remove(threadId);
    }

    public void addFollower (String follower) {
            followers.add(follower);
    }

    public void removeFollower (String follower) {
            followers.remove(follower);
    }


    public void addFollowee (String followee) {
            following.add(followee);
    }

    public void removeFollowee (String followee) {
            following.remove(followee);
    }
}
