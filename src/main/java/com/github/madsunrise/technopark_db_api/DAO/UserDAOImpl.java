package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 08.10.16.
 */
public class UserDAOImpl implements UserDAO {
    private static final Map<Long, User> idToUser = new ConcurrentHashMap<>();
    private static final Map<String, User> emailToUser = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ForumDAOImpl.class.getName());



    @Override
    public User getById(Long id) {
        final User user = idToUser.get(id);
        if (user == null) {
            logger.info("User with id = \"{}\" not found!", id);
            return null;
        }
        return user;
    }

    @Override
    public User getByEmail(String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("User with email = \"{}\" not found!", email);
            return null;
        }
        return user;
    }

    @Override
    public UserDetails create(String username, String name, String email, String about, boolean anonymous) {
        User user = emailToUser.get(email);
        if (user != null) {
            logger.info("Error creating user - user with email \"{}\" already exists!", email);
            return null;
        }

        user = new User(username, name, email, about, anonymous);
        idToUser.put(user.getId(), user);
        emailToUser.put(user.getEmail(), user);
        logger.info("User with email \"{}\" successful created", email);
        return new UserDetails(user);
    }


    @Override
    public UserDetailsExtended getDetails(String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error getting user details - user with email \"{}\" does not exist!", email);
            return null;
        }
        logger.info("Getting user \"{}\" details is success", email);
        return new UserDetailsExtended(user);
    }


    @Override
    public UserDetailsExtended follow(String followerEmail, String followeeEmail) {
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            logger.info("Error following - user does not exits!");
            return null;
        }
        follower.addFollowee(followeeEmail);
        followee.addFollower(followerEmail);
        logger.info("{} now follow {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }


    @Override
    public UserDetailsExtended unfollow(String followerEmail, String followeeEmail) {
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            logger.info("Error unfollowing - user does not exits!");
            return null;
        }
        follower.removeFollowee(followeeEmail);
        followee.removeFollower(followerEmail);
        logger.info("{} now unfollowed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }

    @Override
    public UserDetailsExtended getFollowers(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error get followers - user does not exits!");
            return null;
        }
        final UserDetailsExtended result = new UserDetailsExtended(user);
        result.setFollowers(user.getFollowers(limit, order, sinceId));
        logger.info("Successful getting followers for \"{}\"", email);
        return result;
    }

    @Override
    public UserDetailsExtended getFollowing(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error get followers - user does not exits!");
            return null;
        }
        final UserDetailsExtended result = new UserDetailsExtended(user);
        result.setFollowing(user.getFollowing(limit, order, sinceId));
        logger.info("Successful getting following for \"{}\"", email);
        return result;
    }


    @Override
    public UserDetailsExtended updateProfile(String email, String name, String about) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error update profile - user with email \"{}\" does not exist!", email);
            return null;
        }

        user.setName(name);
        user.setAbout(about);
        // UPDATE 2 COLUMNS..

        emailToUser.put(email, user);
        idToUser.put(user.getId(), user);

        logger.info("Successful updated profile user with email \"{}\"", email);
        return new UserDetailsExtended(user);
    }


    @Override
    public void clear() {
        logger.info("Truncate all users success");
        idToUser.clear();
        emailToUser.clear();
    }

    @Override
    public long getAmount() {
        return idToUser.size();
    }
}
