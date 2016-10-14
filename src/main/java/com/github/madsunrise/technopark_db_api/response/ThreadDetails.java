package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.Thread;

import java.time.LocalDateTime;

/**
 * Created by ivan on 11.10.16.
 */
public class ThreadDetails<F, U> {
    @JsonProperty("id")
    protected long id;
    @JsonProperty("title")
    protected String title;
    @JsonProperty("message")
    protected String message;
    @JsonProperty("date")
    protected String date;
    @JsonProperty("slug")
    protected String slug;
    @JsonProperty("user")
    private U user;
    @JsonProperty("forum")
    private F forum;
    @JsonProperty("isClosed")
    protected boolean closed;
    @JsonProperty("isDeleted")
    protected boolean deleted;

    public ThreadDetails(Thread thread) {
        this.id = thread.getId();
        this.title = thread.getTitle();
        this.message = thread.getMessage();
        this.date = thread.getDateStr();
        this.slug = thread.getSlug();
        this.closed = thread.isClosed();
        this.deleted = thread.isDeleted();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getSlug() {
        return slug;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public F getForum() {
        return forum;
    }

    public void setForum(F forum) {
        this.forum = forum;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
