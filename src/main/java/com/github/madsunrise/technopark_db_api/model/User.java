package com.github.madsunrise.technopark_db_api.model;


import com.github.madsunrise.technopark_db_api.DAO.UserDAOImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<Integer> subscriptions = new ArrayList<>();

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

    public List<String> getFollowers() {
        Collections.sort(followers);
        return followers;
    }

    public List<String> getFollowers (Integer limit, String order, Integer sinceId) {
        if (order == null || !order.equals("asc")) {
            order = "desc";
        }
        else {
            order = "asc";
        }
        List<String> result = new ArrayList<>();

        if (limit == null) {
            limit = followers.size();
        }
        if (sinceId == null) {
            sinceId = -1;
        }

        for (int i = 0; i < followers.size() && i < limit; i++) {
            String email = followers.get(i);
            User user = new UserDAOImpl().getByEmail(email);
            if (user.getId() >= sinceId) {
                result.add(email);
            }
        }
        if (order.equals("asc")) {
            Collections.sort(result);
        }
        else {
            Collections.sort(result, Collections.reverseOrder());
        }
        return result;
    }


    public List<String> getFollowing() {
        Collections.sort(following);
        return following;
    }
    public List<String> getFollowing (Integer limit, String order, Integer sinceId) {
        if (order == null || !order.equals("asc")) {
            order = "desc";
        }
        else {
            order = "asc";
        }
        List<String> result = new ArrayList<>();

        if (limit == null) {
            limit = following.size();
        }
        if (sinceId == null) {
            sinceId = -1;
        }

        for (int i = 0; i < following.size() && i < limit; i++) {
            String email = following.get(i);
            User user = new UserDAOImpl().getByEmail(email);
            if (user.getId() >= sinceId) {
                result.add(email);
            }
        }
        if (order.equals("asc")) {
            Collections.sort(result);
        }
        else {
            Collections.sort(result, Collections.reverseOrder());
        }
        return result;
    }




    public List<Integer> getSubscriptions() {
        Collections.sort(subscriptions);
        return subscriptions;
    }

    public void addFollower (String follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);
        }
    }

    public void removeFollower (String follower) {
        if (followers.contains(follower)) {
            followers.remove(follower);
        }
    }


    public void addFollowee (String followee) {
        if (!following.contains(followee)) {
            following.add(followee);
        }
    }

    public void removeFollowee (String followee) {
        if (following.contains(followee)) {
            following.remove(followee);
        }
    }
}
