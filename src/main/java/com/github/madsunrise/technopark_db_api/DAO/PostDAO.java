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
    long getAmount();
    List<PostDetailsExtended> getPostsDetails(String forumShortName,
                                              LocalDateTime since, Integer limit, String order);
    List<PostDetailsExtended> getPostsDetails(long threadId,
                                              LocalDateTime since, Integer limit, String order);
    Long remove (long postId);
    void markDeleted(long threadId);
}
