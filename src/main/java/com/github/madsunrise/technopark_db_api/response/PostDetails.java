package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.Post;

/**
 * Created by ivan on 12.10.16.
 */
public class PostDetails<F, U, T> {
    @JsonProperty("id")
    protected long id;
    @JsonProperty("date")
    protected String date;
    @JsonProperty("forum")
    protected F forum;
    @JsonProperty("user")
    private U user;
    @JsonProperty("message")
    protected String message;

    @JsonProperty("parent")
    protected Long parent;
    @JsonProperty("thread")
    protected T thread;

    @JsonProperty("isApproved")
    protected boolean approved;
    @JsonProperty("isDeleted")
    protected boolean deleted;
    @JsonProperty("isEdited")
    protected boolean edited;
    @JsonProperty("isHighlighted")
    protected boolean highlighted;
    @JsonProperty("isSpam")
    protected boolean spam;

    public PostDetails (Post post) {
        this.id = post.getId();
        this.date = post.getDateStr();
        this.message = post.getMessage();
        this.parent = post.getParent();
        this.approved = post.isApproved();
        this.deleted = post.isDeleted();
        this.edited = post.isEdited();
        this.highlighted = post.isHighlighted();
        this.spam = post.isSpam();
    }


    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public F getForum() {
        return forum;
    }

    public U getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public Long getParent() {
        return parent;
    }

    public T getThread() {
        return thread;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public boolean isSpam() {
        return spam;
    }


    public void setForum(F forum) {
        this.forum = forum;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public void setThread(T thread) {
        this.thread = thread;
    }
}
