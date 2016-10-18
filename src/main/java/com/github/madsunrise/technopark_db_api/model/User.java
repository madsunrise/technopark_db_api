package com.github.madsunrise.technopark_db_api.model;


import java.util.*;

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
    private List<String> followers = new ArrayList<>();
    private List<String> followees = new ArrayList<>();
    private List<Long> subscriptions = new ArrayList<>();

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

    public Boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowees() {
        return followees;
    }

    public void setFollowees(List<String> followees) {
        this.followees = followees;
    }

    public List<Long> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Long> subscriptions) {
        this.subscriptions = subscriptions;
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
            followees.add(followee);
    }

    public void removeFollowee (String followee) {
            followees.remove(followee);
    }
}
