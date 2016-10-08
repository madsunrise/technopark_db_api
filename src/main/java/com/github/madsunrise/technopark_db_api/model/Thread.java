package com.github.madsunrise.technopark_db_api.model;

import java.time.LocalDateTime;

/**
 * Created by ivan on 08.10.16.
 */
public class Thread {
    private long id;
    private String title;
    private String message;
    private LocalDateTime date;
    private String slug;
    private String user;
    private long userId;
    private String forum;
    private String forumId;
    private boolean closed;
    private boolean deleted;

    public Thread(String title, String message, LocalDateTime date, String slug, String user, String forum, boolean closed) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.slug = slug;
        this.user = user;
        this.forum = forum;
        this.closed = closed;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
