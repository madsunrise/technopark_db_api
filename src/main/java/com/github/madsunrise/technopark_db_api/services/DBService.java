package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.builder.ForumBuilder;
import com.github.madsunrise.technopark_db_api.builder.PostBuilder;
import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;

import java.util.List;

/**
 * Created by ivan on 08.10.16.
 */
public interface DBService {
    CustomResponse clear();
    CustomResponse status();
    CustomResponse createForum(String name, String shortName, String user);
    CustomResponse getForumDetails(String shortName);
    CustomResponse getForumDetails(String shortName, List<String> related);
    CustomResponse getForumPosts (String shortName, String since, int limit, String order, List<String> related);
    CustomResponse getForumThreads (String shortName, String since, int limit, String order, List<String> related);
    CustomResponse getForumUsers (String shortName, int limit, String order, int sinceId);

    PostBuilder createPost(String message, String date, int threadId, String user, long userId,
                           String forum, long forumId);

    CustomResponse getPostsList (String forum);
    CustomResponse getPostsList (int threadId);
}
