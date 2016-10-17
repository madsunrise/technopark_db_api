package com.github.madsunrise.technopark_db_api.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ivan on 08.10.16.
 */
public class Forum {
    private long id;
    private String name;
    private String shortName;
    private String user;
    private long userId;

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);

    public Forum(String name, String shortName, String user, long userId) {
        this.name = name;
        this.shortName = shortName;
        this.user = user;
        this.userId = userId;
        this.id = ID_GENETATOR.getAndIncrement();
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
