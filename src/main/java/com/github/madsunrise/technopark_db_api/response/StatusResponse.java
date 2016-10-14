package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ivan on 13.10.16.
 */
public class StatusResponse {
    @JsonProperty("user")
    private long userCount;
    @JsonProperty("thread")
    private long threadCount;
    @JsonProperty("forum")
    private long forumCount;
    @JsonProperty("post")
    private long postCount;

    public StatusResponse(long userCount, long threadCount, long forumCount, long postCount) {
        this.userCount = userCount;
        this.threadCount = threadCount;
        this.forumCount = forumCount;
        this.postCount = postCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getThreadCount() {
        return threadCount;
    }

    public long getForumCount() {
        return forumCount;
    }

    public long getPostCount() {
        return postCount;
    }
}
