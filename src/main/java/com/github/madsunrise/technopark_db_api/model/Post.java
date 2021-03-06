package com.github.madsunrise.technopark_db_api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ivan on 08.10.16.
 */


public class Post {
    private long id;
    private String message;
    private LocalDateTime date;
    private long threadId;
    private long userId;
    private long forumId;
    private Long parent;
    private long root;
    private boolean approved;
    private boolean highlighted;
    private boolean edited;
    private boolean spam;
    private boolean deleted;

    private int likes;
    private int dislikes;

    private String path;

    public Post(String message, LocalDateTime date, long threadId, long userId, long forumId,
                Long parent, boolean approved, boolean highlighted, boolean edited, boolean spam, boolean deleted) {
        this.message = message;
        this.date = date;
        this.threadId = threadId;
        this.userId = userId;
        this.forumId = forumId;
        this.parent = parent;
        this.approved = approved;
        this.highlighted = highlighted;
        this.edited = edited;
        this.spam = spam;
        this.deleted = deleted;
    }

    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
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

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getForumId() {
        return forumId;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean isSpam() {
        return spam;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getPoints() {
        return likes - dislikes;
    }

    public long getRoot() {
        return root;
    }

    public void setRoot(long root) {
        this.root = root;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
