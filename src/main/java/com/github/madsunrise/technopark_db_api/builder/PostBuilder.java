package com.github.madsunrise.technopark_db_api.builder;

import com.github.madsunrise.technopark_db_api.model.Post;

import java.time.LocalDateTime;

/**
 * Created by ivan on 08.10.16.
 */
public class PostBuilder {
    private final String message;
    private final LocalDateTime date;
    private final long threadId;
    private final String user;
    private final long userId;
    private final String forum;
    private final long forumId;
    private Long parent = null;
    private boolean approved = false;
    private boolean highlighted = false;
    private boolean edited = false;
    private boolean spam = false;
    private boolean deleted = false;

    public PostBuilder(String message, LocalDateTime date, long threadId,
                       String user, long userId, String forum, long forumId) {
        this.message = message;
        this.date = date;
        this.threadId = threadId;
        this.user = user;
        this.userId = userId;
        this.forum = forum;
        this.forumId = forumId;
    }

    public PostBuilder parent(Long parent) {
        this.parent = parent;
        return this;
    }

    public PostBuilder approved(boolean approved) {
        this.approved = approved;
        return this;
    }

    public PostBuilder highlighted(boolean highlighted) {
        this.highlighted = highlighted;
        return this;
    }

    public PostBuilder edited(boolean edited) {
        this.edited = edited;
        return this;
    }

    public PostBuilder spam(boolean spam) {
        this.spam = spam;
        return this;
    }

    public PostBuilder deleted (boolean deleted) {
        this.deleted = deleted;
        return this;
    }


    public Post build()
    {
        return new Post(message, date, threadId, user, userId,
                forum, forumId, parent, approved, highlighted, edited, spam, deleted);
    }
}

