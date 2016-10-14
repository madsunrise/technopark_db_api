package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;

/**
 * Created by ivan on 08.10.16.
 */
public interface UserDAO {
    User getById (Long id);
    User getByEmail (String email);
    UserDetails create(String username, String name, String email, String about, boolean anonymous);
    UserDetailsExtended getDetails (String email);
    UserDetailsExtended follow (String followerEmail, String followeeEmail);
    UserDetailsExtended unfollow (String followerEmail, String followeeEmail);
    UserDetailsExtended getFollowers (String email, Integer limit, String order, Integer sinceId);
    UserDetailsExtended getFollowing (String email, Integer limit, String order, Integer sinceId);
    UserDetailsExtended updateProfile (String email, String name, String about);
    void clear();
    long getAmount();
    Long subscribe (long threadId, String email);
    Long unsubscribe (long threadId, String email);
}
