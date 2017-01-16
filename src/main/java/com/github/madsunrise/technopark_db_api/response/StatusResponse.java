package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ivan on 13.10.16.
 */
public class StatusResponse {
    @JsonProperty("user")
    private int userCount;
    @JsonProperty("thread")
    private int threadCount;
    @JsonProperty("forum")
    private int forumCount;
    @JsonProperty("post")
    private int postCount;

    public StatusResponse(int userCount, int threadCount, int forumCount, int postCount) {
        this.userCount = userCount;
        this.threadCount = threadCount;
        this.forumCount = forumCount;
        this.postCount = postCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getForumCount() {
        return forumCount;
    }

    public int getPostCount() {
        return postCount;
    }
}
