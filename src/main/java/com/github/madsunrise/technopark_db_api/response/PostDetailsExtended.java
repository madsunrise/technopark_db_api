package com.github.madsunrise.technopark_db_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.model.Post;

/**
 * Created by ivan on 12.10.16.
 */
public class PostDetailsExtended extends PostDetails {
    @JsonProperty("likes")
    private int likes;
    @JsonProperty("dislikes")
    private int dislikes;
    @JsonProperty("points")
    private int points;

    public PostDetailsExtended(Post post) {
        super(post);
        likes = post.getLikes();
        dislikes = post.getDislikes();
        points = post.getPoints();
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
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
