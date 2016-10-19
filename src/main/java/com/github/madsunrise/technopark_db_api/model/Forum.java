package com.github.madsunrise.technopark_db_api.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ivan on 08.10.16.
 */
public class Forum {
    private long id;
    private String name;
    private String shortName;
    private long userId;

    public Forum(String name, String shortName, long userId) {
        this.name = name;
        this.shortName = shortName;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
