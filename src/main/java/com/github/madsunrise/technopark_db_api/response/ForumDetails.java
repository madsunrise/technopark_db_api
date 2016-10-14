package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.Forum;

/**
 * Created by ivan on 09.10.16.
 */
public class ForumDetails<V> {
    @JsonProperty("id")
    private long id;
    @JsonProperty("name")
    private String name;

    @JsonProperty("short_name")
    private String shortName;
    @JsonProperty("user")
    private V user;

    public ForumDetails(long id, String name, String shortName, V user) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.user = user;
    }

    public ForumDetails(Forum forum) {
        this.id = forum.getId();
        this.name = forum.getName();
        this.shortName = forum.getShortName();
    }



    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public V getUser() {
        return user;
    }

    public void setUser(V user) {
        this.user = user;
    }
}
