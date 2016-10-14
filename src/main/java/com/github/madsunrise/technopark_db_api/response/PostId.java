package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ivan on 14.10.16.
 */
public class PostId {
    @JsonProperty("post")
    private final long id;

    public PostId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
