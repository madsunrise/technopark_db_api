package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class UserDAO {

    private final JdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class.getName());
    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private ForumDAO forumDAO;

    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS user";
        template.execute(dropTable);
        LOGGER.info("Table user was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE user (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100)," +
                "username VARCHAR(30)," +
                "email VARCHAR(30) NOT NULL," +
                "about TEXT," +
                "anonymous TINYINT(1) NOT NULL DEFAULT 0," +
                "UNIQUE KEY (email)) CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci";
        template.execute(createTable);
        LOGGER.info("Table user was created");
    }

    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM user;";
        return template.queryForObject(query, Long.class);
    }


    public User getById (long userId) {
        try {
            final User user = template.queryForObject(
                    "SELECT * FROM user WHERE id = ?", userMapper, userId);

            user.setFollowers(loadFollowers(user.getId()));
            user.setFollowees(loadFollowees(user.getId()));
            user.setSubscriptions(loadSubscriptions(user.getId()));
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getByEmail(String email) {
        try {
            final User user = template.queryForObject(
                    "SELECT * FROM user WHERE email = ?", userMapper, email);

            user.setFollowers(loadFollowers(user.getId()));
            user.setFollowees(loadFollowees(user.getId()));
            user.setSubscriptions(loadSubscriptions(user.getId()));
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UserDetails create(String username, String name, String email, String about, boolean anonymous) {
        final long start = System.currentTimeMillis();
        final User user = new User(username, name, email, about, anonymous);
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new UserPstCreator(user), keyHolder);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating user - user with email \"{}\" already exists!", email);
            return null;
        }
        final Map<String, Object> keys = keyHolder.getKeys();
        user.setId((Long)keys.get("GENERATED_KEY"));
        final long end = System.currentTimeMillis();
//        LOGGER.info("User with email \"{}\" successful created, time: {}", email, end-start);
        return new UserDetails(user);
    }

        private static class UserPstCreator implements PreparedStatementCreator {
            private final User user;
            UserPstCreator(User user) {
                this.user = user;
            }
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                final String query = "INSERT INTO user (username, name, email, about, anonymous) VALUES (?,?,?,?,?);";
                final PreparedStatement pst = con.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS);
                pst.setString(1, user.getUsername());
                pst.setString(2, user.getName());
                pst.setString(3, user.getEmail());
                pst.setString(4, user.getAbout());
                pst.setString(5, user.isAnonymous()? "1": "0");
                return pst;
            }
        }


    public UserDetailsExtended getDetails(long userId) {
        final User user = getById(userId);
        if (user == null) {
            LOGGER.info("Error getting user details - user with ID=\"{}\" does not exist!", userId);
            return null;
        }
  //      LOGGER.info("Getting user with ID=\"{}\" details is success", userId);
        return new UserDetailsExtended(user);

    }


    public UserDetailsExtended getDetails(String email) {
        final User user = getByEmail(email);
        if (user == null) {
            LOGGER.info("Error getting user details - user with email \"{}\" does not exist!", email);
            return null;
        }
    //    LOGGER.info("Getting user \"{}\" details is success", email);
        return new UserDetailsExtended(user);

    }

    public UserDetailsExtended follow(String followerEmail, String followeeEmail) {
        if (followerEmail.equals(followeeEmail)) {
            LOGGER.info("Error following!");
            return null;
        }
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            LOGGER.info("Error following!");
            return null;
        }

        try {
            final String query = "INSERT INTO following (follower_id, followee_id) VALUES (?,?);";
            template.update(query, follower.getId(), followee.getId());
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("User {} already followed user {}!", followerEmail, followeeEmail);
            follower.addFollowee(followeeEmail);
            return new UserDetailsExtended(follower);
        }
      //  LOGGER.info("{} has followed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }

    public UserDetailsExtended unfollow(String followerEmail, String followeeEmail) {
        if (followerEmail.equals(followeeEmail)) {
            LOGGER.info("Error unfollowing!");
            return null;
        }

        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            LOGGER.info("Error unfollowing - user does not exist!");
            return null;
        }
        follower.removeFollowee(followeeEmail);

        final String query = "DELETE FROM following WHERE follower_id = ? AND followee_id = ?;";

        template.update(query, follower.getId(), followee.getId());

      //  LOGGER.info("{} has unfollowed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }


    public List<UserDetailsExtended> getFollowers(String email, String order) {
        final String query = "SELECT follower.id, follower.username, follower.name," +
                " follower.email, follower.about, follower.anonymous" +
                " FROM user as follower JOIN following f ON follower.id = f.follower_id " +
                "JOIN user ON user.id = f.followee_id " +
                " WHERE user.email = ? ORDER BY name " + order + ';';

        final List<User> users = template.query(query, userMapper, email);
        //LOGGER.info("Getting followers for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowers(String email, int sinceId, String order) {
        final String query = "SELECT follower.id, follower.username," +
                " follower.name, follower.email, follower.about, follower.anonymous" +
                " FROM user as follower JOIN following f ON follower.id = f.follower_id " +
                "JOIN user ON user.id = f.followee_id " +
                " WHERE user.email = ? AND follower.id >= ? ORDER BY name " + order + ';';

        final List<User> users = template.query(query, userMapper, email, sinceId);
        //LOGGER.info("Getting followers for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowers(String email, String order, int limit) {
        final String query = "SELECT follower.id, follower.username, follower.name, " +
                "follower.email, follower.about, follower.anonymous" +
                " FROM user as follower JOIN following f ON follower.id = f.follower_id " +
                "JOIN user ON user.id = f.followee_id " +
                " WHERE user.email = ? ORDER BY name " + order + " LIMIT ?";

        final List<User> users = template.query(query, userMapper, email, limit);
        //LOGGER.info("Getting followers for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowers(String email, int sinceId, String order, int limit) {
        final String query = "SELECT follower.id, follower.username, follower.name, follower.email," +
                " follower.about, follower.anonymous" +
                " FROM user as follower JOIN following f ON follower.id = f.follower_id " +
                "JOIN user ON user.id = f.followee_id " +
                " WHERE user.email = ? AND follower.id >= ? ORDER BY name " + order + " LIMIT ?";

        final List<User> users = template.query(query, userMapper, email, sinceId, limit);
        //LOGGER.info("Getting followers for user {} is success", email);
        return usersToUsersDetails(users);
    }



    public List<UserDetailsExtended> getFollowees(String email, String order) {
        final String query = "SELECT followee.id, followee.username, followee.name, followee.email," +
                " followee.about, followee.anonymous" +
                " FROM user as followee JOIN following f ON followee.id = f.followee_id " +
                "JOIN user ON user.id = f.follower_id " +
                " WHERE user.email = ? ORDER BY name " + order + ';';

        final List<User> users = template.query(query, userMapper, email);
        //LOGGER.info("Getting followees for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowees(String email, int sinceId, String order) {
        final String query = "SELECT followee.id, followee.username, followee.name, followee.email," +
                " followee.about, followee.anonymous" +
                " FROM user as followee JOIN following f ON followee.id = f.followee_id " +
                "JOIN user ON user.id = f.follower_id " +
                " WHERE user.email = ? AND followee.id >= ? ORDER BY name " + order + ';';

        final List<User> users = template.query(query, userMapper, email, sinceId);
        //LOGGER.info("Getting followees for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowees(String email, String order, int limit) {
        final String query = "SELECT followee.id, followee.username, followee.name, followee.email," +
                " followee.about, followee.anonymous" +
                " FROM user as followee JOIN following f ON followee.id = f.followee_id " +
                "JOIN user ON user.id = f.follower_id " +
                " WHERE user.email = ? ORDER BY name " + order + " LIMIT ?";

        final List<User> users = template.query(query, userMapper, email, limit);
        //LOGGER.info("Getting followees for user {} is success", email);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getFollowees(String email, int sinceId, String order, int limit) {
        final String query = "SELECT followee.id, followee.username, followee.name, followee.email," +
        " followee.about, followee.anonymous" +
                " FROM user as followee JOIN following f ON followee.id = f.followee_id " +
                "JOIN user ON user.id = f.follower_id " +
                " WHERE user.email = ? AND followee.id >= ? ORDER BY name " + order + " LIMIT ?";

        final List<User> users = template.query(query, userMapper, email, sinceId, limit);
        //LOGGER.info("Getting followees for user {} is success", email);
        return usersToUsersDetails(users);
    }



    public UserDetailsExtended updateProfile(String email, String name, String about) {
        final String query = "UPDATE user SET name = ?, about = ? WHERE email = ?;";
        final int affectedRows = template.update(query, name, about, email);
        if (affectedRows == 0) {
            LOGGER.info("Error update user profile because user with email {} does not exist!", email);
            return null;
        }
        final User user = getByEmail(email);
        //LOGGER.info("User profile has been updated (email {})", email);
        return new UserDetailsExtended(user);
    }

    public Long subscribe(long threadId, String email) {
        final User user = getByEmail(email);
        if (user == null) {
            return null;
        }
        final Long userId = user.getId();

        try {
            final String query = "INSERT INTO subscription (user_id, thread_id) VALUES (?, ?);";
            template.update(query, userId, threadId);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("User {} already subscribed on thread with id={}!", email, threadId);
            return userId;
        }
        //LOGGER.info("User {} has subscribed on thread with id={}", email, threadId);
        return userId;
    }

    public Long unsubscribe(long threadId, String email) {
        final User user = getByEmail(email);
        if (user == null) {
            return null;
        }
        final Long userId = user.getId();
        final String query = "DELETE FROM subscription WHERE user_id=? AND thread_id=?;";
            template.update(query, userId, threadId);

        //LOGGER.info("User {} has unsubscribed fro, thread with id={}", email, threadId);
        return userId;
    }

    public List<PostDetailsExtended> getPosts(String email, LocalDateTime since, Integer limit, String order) {
        if (since == null) {
            if (limit == null) {
                return postDAO.getPostsByUser(email, order);
            }
            else {
                return postDAO.getPostsByUser(email, limit, order);
            }
        }
        else {
            if (limit == null) {
                return postDAO.getPostsByUser(email, since, order);
            }
            else {
                return postDAO.getPostsByUser(email, since, limit, order);
            }
        }
    }



    public List<UserDetailsExtended> getUsersByForum(String forumShortName, String order) {
        final long forumId = forumDAO.getByShortName(forumShortName).getId();
        final String query = "SELECT u.*" +
                " FROM user u JOIN user_forum uf ON u.id = uf.user_id " +
                "WHERE uf.forum_id = ? ORDER BY u.name " + order + ';';
        final List<User> users = template.query(query, userMapper, forumId);
        //LOGGER.info("Getting users of forum {} is successful", forumShortName);
        return usersToUsersDetails(users);
    }


    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, String order) {
        final long forumId = forumDAO.getByShortName(forumShortName).getId();
        final String query = "SELECT u.*" +
                " FROM user u JOIN user_forum uf ON u.id = uf.user_id " +
                "WHERE uf.forum_id = ?" +
                " AND u.id >= ?  ORDER BY u.name " + order + ';';
        final List<User> users = template.query(query, userMapper, forumId, sinceId);
        //LOGGER.info("Getting users of forum {} is successful", forumShortName);
        return usersToUsersDetails(users);
    }



    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Integer limit, String order) {
        final long forumId = forumDAO.getByShortName(forumShortName).getId();
        final String query = "SELECT u.*" +
                " FROM user u JOIN user_forum uf ON u.id = uf.user_id " +
                "WHERE uf.forum_id = ?" +
                " ORDER BY u.name " + order + " LIMIT ?";
        final List<User> users = template.query(query, userMapper, forumId, limit);
    //    LOGGER.info("Getting users of forum {} is successful", forumShortName);
        return usersToUsersDetails(users);
    }

    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, Integer limit, String order) {
        final long forumId = forumDAO.getByShortName(forumShortName).getId();
        final String query = "SELECT u.*" +
                " FROM user u JOIN user_forum uf ON u.id = uf.user_id " +
                "WHERE uf.forum_id = ?" +
                " AND u.id >= ?  ORDER BY name " + order + " LIMIT ?";
        final List<User> users = template.query(query, userMapper, forumId, sinceId, limit);
      //  LOGGER.info("Getting users of forum {} is successful", forumShortName);
        return usersToUsersDetails(users);
    }



    private List<UserDetailsExtended> usersToUsersDetails (Iterable<User> users) {
        final List<UserDetailsExtended> result = new ArrayList<>();
        for (User user: users) {
            user.setFollowers(loadFollowers(user.getId()));
            user.setFollowees(loadFollowees(user.getId()));
            user.setSubscriptions(loadSubscriptions(user.getId()));
            result.add(new UserDetailsExtended(user));
        }
        return result;
    }

    private List<String> loadFollowers (long followeeId) {
        final String getFollowers = "SELECT u.email FROM user as u JOIN following f " +
                "ON u.id = f.follower_id WHERE followee_id = ?;";
        return template.queryForList(getFollowers, String.class, followeeId);
    }

    private List<String> loadFollowees (long followerId) {
        final String getFollowees = "SELECT u.email FROM user as u JOIN following f " +
                "ON u.id = f.followee_id WHERE follower_id = ?;";
        return template.queryForList(getFollowees, String.class, followerId);
    }

    private List<Long> loadSubscriptions(long userId) {
        final String getSubscriptions = "SELECT thread_id FROM subscription WHERE user_id = ?;";
        return template.queryForList(getSubscriptions, Long.class, userId);
    }

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        final String username = rs.getString("username");
        final String name = rs.getString("name");
        final String eMail = rs.getString("email");
        final String about = rs.getString("about");
        final long id = rs.getLong("id");
        final boolean anonymous = rs.getBoolean("anonymous");
        final User result = new User(username, name, eMail, about, anonymous);
        result.setId(id);
        return result;
    };
}
