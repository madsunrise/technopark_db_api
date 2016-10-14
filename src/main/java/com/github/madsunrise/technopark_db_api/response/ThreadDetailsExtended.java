package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.Thread;

/**
 * Created by ivan on 11.10.16.
 */
public class ThreadDetailsExtended extends ThreadDetails {
    @JsonProperty("likes")
    private int likes;
    @JsonProperty("dislikes")
    private int dislikes;
    @JsonProperty("points")
    private int points;
    @JsonProperty("posts")
    private int posts;

    public ThreadDetailsExtended(Thread thread) {
        super(thread);
        likes = thread.getLikes();
        dislikes = thread.getDislikes();
        points = thread.getPoints();
        posts = thread.getPosts();
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getPoints() {
        return points;
    }

    public int getPosts() {
        return posts;
    }

}
