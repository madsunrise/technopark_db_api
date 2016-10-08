package com.github.madsunrise.technopark_db_api.builder;

import com.github.madsunrise.technopark_db_api.model.Forum;

/**
 * Created by ivan on 08.10.16.
 */
public class ForumBuilder {
    private final String name;
    private final String shortName;
    private final String user;
    private final long userId;

    public ForumBuilder(String name, String shortName, String user, long userId) {
        this.name = name;
        this.shortName = shortName;
        this.user = user;
        this.userId = userId;
    }

    public Forum build() {
        return new Forum(name, shortName, user, userId);
    }
}
