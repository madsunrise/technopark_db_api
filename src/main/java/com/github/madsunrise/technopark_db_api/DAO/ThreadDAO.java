package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetails;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ivan on 11.10.16.
 */
public interface ThreadDAO {
    Thread getById (long id);
    ThreadDetails create (String forum, String title, boolean closed, String userEmail, LocalDateTime date,
                          String message, String slug, /*optional*/ boolean deleted);
    boolean close (long threadId);
    boolean open (long threadId);
    ThreadDetailsExtended getDetails (long threadId);
    ThreadDetailsExtended getDetails (long threadId, List<String> related);
    void clear();
    void createTable();
    long getAmount();
    long save (Thread thread);
    void addPost(long threadId);
    boolean remove (long threadId);
    boolean restore (long threadId);
    Long subscribe (long threadId, String userEmail);
    Long unsubscribe (long threadId, String userEmail);
    List<PostDetailsExtended> getPosts (long threadId, LocalDateTime since, Integer limit, String order, String sort);
    ThreadDetailsExtended vote(long threadId, int vote);
    ThreadDetailsExtended update (long threadId, String message, String slug);
    List <ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since,
                                                   Integer limit, String order, List<String> related);
    List <ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since,
                                                   Integer limit, String order);
    List <ThreadDetailsExtended> getThreadsByUser(String userEmail, LocalDateTime since,
                                                  Integer limit, String order);
}
