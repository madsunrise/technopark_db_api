package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ivan on 09.10.16.
 */
public interface ForumDAO {
    Forum getByShortName (String shortName);
    ForumDetails create(String name, String shortName, String user);
    ForumDetails getDetails(String shortName);
    ForumDetails getDetails(String shortName, String related);
    void clear();
    void createTable();
    long getAmount();
    List<PostDetailsExtended> getPosts(String shortName, LocalDateTime since,
                                        Integer limit, String order, List<String> related);
    List<ThreadDetailsExtended> getThreads(String shortName,LocalDateTime since,
                                           Integer limit, String order, List<String> related);
    List<UserDetailsExtended> getUsers (String shortName, Long sinceId, Integer limit, String order);
}
