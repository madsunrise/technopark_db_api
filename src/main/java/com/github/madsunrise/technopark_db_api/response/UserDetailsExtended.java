package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 09.10.16.
 */
public class UserDetailsExtended extends UserDetails {
    @JsonProperty("followers")
    private List<String> followers = new ArrayList<>();
    @JsonProperty("following")
    private List<String> following = new ArrayList<>();
    @JsonProperty("subscriptions")
    private List<Long> subscriptions = new ArrayList<>();

    public UserDetailsExtended() {
    }

    public UserDetailsExtended(long id, String name, String username, String about, String email, boolean anonymous) {
        super(id, name, username, about, email, anonymous);
    }

    public UserDetailsExtended(User user) {
        super(user);
        this.followers = user.getFollowers();
        this.following = user.getFollowees();
        this.subscriptions = user.getSubscriptions();
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public List<Long> getSubscriptions() {
        return subscriptions;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public void setSubscriptions(List<Long> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
