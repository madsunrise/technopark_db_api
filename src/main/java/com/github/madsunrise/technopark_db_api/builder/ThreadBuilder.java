package com.github.madsunrise.technopark_db_api.builder;

import com.github.madsunrise.technopark_db_api.model.Thread;

import java.time.LocalDateTime;

/**
 * Created by ivan on 08.10.16.
 */
public class ThreadBuilder {
    private final String title;
    private final String message;
    private final LocalDateTime date;
    private final String slug;
    private final String user;
    private final long userId;
    private final String forum;
    private final long forumId;
    private final boolean closed;
    private boolean deleted = false;


    public ThreadBuilder(String title, String message, LocalDateTime date,
                         String slug, String user, long userId, String forum, long forumId, boolean closed) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.slug = slug;
        this.user = user;
        this.userId = userId;
        this.forum = forum;
        this.forumId = forumId;
        this.closed = closed;
    }

    public ThreadBuilder deleted (boolean deleted) {
        this.deleted = deleted;
        return this;
    }


    public Thread build()
    {
        return new Thread(title, message, date, slug, user, userId, forum, forumId, closed, deleted);
    }
}