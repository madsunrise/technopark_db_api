package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ivan on 08.10.16.
 */
public interface UserDAO {
    User getByEmail (String email);
    UserDetails create(String username, String name, String email, String about, boolean anonymous);
    UserDetailsExtended getDetails (String email);
    UserDetailsExtended follow (String followerEmail, String followeeEmail);
    UserDetailsExtended unfollow (String followerEmail, String followeeEmail);
    List<UserDetailsExtended> getFollowers (String email, String order);
    List<UserDetailsExtended> getFollowers (String email, int sinceId, String order);
    List<UserDetailsExtended> getFollowers (String email, String order, int limit);
    List<UserDetailsExtended> getFollowers (String email, int sinceId, String order, int limit);
    List<UserDetailsExtended> getFollowees(String email, String order);
    List<UserDetailsExtended> getFollowees(String email, int sinceId, String order);
    List<UserDetailsExtended> getFollowees(String email, String order, int limit);
    List<UserDetailsExtended> getFollowees(String email, int sinceId, String order, int limit);
    UserDetailsExtended updateProfile (String email, String name, String about);
    void clear();
    void createTable();
    long getAmount();
    Long subscribe (long threadId, String email);
    Long unsubscribe (long threadId, String email);
    List<PostDetailsExtended> getPosts(String email, LocalDateTime since, Integer limit, String order);
    List<UserDetailsExtended> getUsersByForum (String forumShortName, Long sinceId, Integer limit, String order);
}
