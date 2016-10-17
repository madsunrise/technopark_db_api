package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Post;
import com.github.madsunrise.technopark_db_api.response.PostDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ivan on 12.10.16.
 */
public interface PostDAO {
    Post getById(long id);
    PostDetails create(LocalDateTime date, long threadId, String message, String userEmail, String forumShortName,
                       Long parent, boolean approved, boolean highlighted, boolean edited, boolean spam,
                       boolean delete);
    PostDetailsExtended getDetails(long id, List<String> related);
    void clear();
    void createTable();
    long getAmount();
    List<PostDetailsExtended> getPostsByForum(String forumShortName,
                                              LocalDateTime since, Integer limit, String order);
    List<PostDetailsExtended> getPostsByForum(String forumShortName,
                                              LocalDateTime since, Integer limit, String order, List<String> related);
    List<PostDetailsExtended> getPostsByThread(long threadId,
                                               LocalDateTime since, Integer limit, String order);

    List<PostDetailsExtended> getPostsByUser(String userEmail,
                                             LocalDateTime since, Integer limit, String order);

    Long remove (long postId);
    Long restore (long postId);
    void markDeleted(long threadId);
    void markRestored (long threadId);
    PostDetailsExtended vote(long postId, int vote);
    PostDetailsExtended update (long postId, String message);
}
