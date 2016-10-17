package com.github.madsunrise.technopark_db_api.model;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

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
    private long forumId;
    private boolean closed;
    private boolean deleted;
    private int likes;
    private int dislikes;
    private int posts;

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);

    public Thread(String title, String message, LocalDateTime date, String slug,
                  String user, long userId, String forum, long forumId, boolean closed, boolean deleted) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.slug = slug;
        this.user = user;
        this.userId = userId;
        this.forum = forum;
        this.forumId = forumId;
        this.closed = closed;
        this.deleted = deleted;

        this.id = ID_GENETATOR.getAndIncrement();
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

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateStr() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date.format(formatter);
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

    public long getForumId() {
        return forumId;
    }

    public void setForumId(long forumId) {
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

    public void setId(long id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }


    public int getDislikes() {
        return dislikes;
    }


    public int getPoints() {
        return likes - dislikes;
    }


    public int getPosts() {
        if (deleted) {
            return 0;
        }
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public void addPost() {
        posts++;
    }
    public void removePost() {
        posts--;
    }

    public void like() {
        likes++;
    }
    public void dislike() {
        dislikes++;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }
}
