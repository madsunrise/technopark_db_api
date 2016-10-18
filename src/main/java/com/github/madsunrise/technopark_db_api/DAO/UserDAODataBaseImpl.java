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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class UserDAODataBaseImpl implements UserDAO {

    private final JdbcTemplate template;
    private static final Logger logger = LoggerFactory.getLogger(UserDAODataBaseImpl.class.getName());
    public UserDAODataBaseImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private PostDAODataBaseImpl postDAODataBase;

    @Override
    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS user";
        template.execute(dropTable);
        logger.info("Table user was dropped");
    }

    @Override
    public void createTable() {
        final String createTable = "CREATE TABLE user (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100)," +
                "username VARCHAR(30)," +
                "email VARCHAR(30) UNIQUE NOT NULL," +
                "about TEXT," +
                "anonymous TINYINT(1) NOT NULL DEFAULT 0) CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;";
        template.execute(createTable);
        logger.info("Table user was created");
    }

    @Override
    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM user;";
        return template.queryForObject(query, Long.class);
    }


    @Override
    public User getByEmail(String email) {
        try {
            final User user = template.queryForObject(
                    "SELECT * FROM user WHERE email = ?", userMapper, email);

            final String getFollowers = "SELECT follower_email FROM following WHERE followee_id=?;";
            final List<String> followers = template.queryForList(getFollowers, String.class, user.getId());

            final String getFollowees = "SELECT followee_email FROM following WHERE follower_id=?;";
            final List<String> followees = template.queryForList(getFollowees, String.class, user.getId());

            final String getSubscriptions = "SELECT thread_id FROM subscription WHERE user_id = ?;";
            final List<Long> subscriptions = template.queryForList(getSubscriptions, Long.class, user.getId());

            user.setFollowers(new HashSet<>(followers));
            user.setFollowing(new HashSet<>(followees));
            user.setSubscriptions(new HashSet<>(subscriptions));
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public UserDetails create(String username, String name, String email, String about, boolean anonymous) {
        final User user = new User(username, name, email, about, anonymous);
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new UserPstCreator(user), keyHolder);
        }
        catch (DuplicateKeyException e) {
            logger.info("Error creating user - user with email \"{}\" already exists!", email);
            return null;
        }
        final Map<String, Object> keys = keyHolder.getKeys();
        user.setId((Long)keys.get("GENERATED_KEY"));
        logger.info("User with email \"{}\" successful created", email);
        return new UserDetails(user);
    }

        private static class UserPstCreator implements PreparedStatementCreator {
            private final User user;
            UserPstCreator(User user) {
                this.user = user;
            }
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                final String query = "INSERT INTO user (username, name, email, about, anonymous) VALUES (?,?,?,?,?);";
                PreparedStatement pst = con.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS);
                pst.setString(1, user.getUsername());
                pst.setString(2, user.getName());
                pst.setString(3, user.getEmail());
                pst.setString(4, user.getAbout());
                pst.setString(5, user.isAnonymous()? "1": "0");
                return pst;
            }
        }

    @Override
    public UserDetailsExtended getDetails(String email) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error getting user details - user with email \"{}\" does not exist!", email);
            return null;
        }
        logger.info("Getting user \"{}\" details is success", email);
        return new UserDetailsExtended(user);

    }

    @Override
    public UserDetailsExtended follow(String followerEmail, String followeeEmail) {
        if (followerEmail.equals(followeeEmail)) {
            logger.info("Error following!");
            return null;
        }
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            logger.info("Error following!");
            return null;
        }
        final String query = "INSERT INTO following (follower_id, follower_email," +
                " followee_id, followee_email) VALUES (?,?,?,?);";
        try {
            template.update(query, follower.getId(), followerEmail, followee.getId(), followeeEmail);
        }
        catch (DuplicateKeyException e) {
            logger.info("User {} already followed user {}!", followerEmail, followeeEmail);
            follower.addFollowee(followeeEmail);
            return new UserDetailsExtended(follower);
        }
        logger.info("{} has followed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }

    @Override
    public UserDetailsExtended unfollow(String followerEmail, String followeeEmail) {
        if (followerEmail.equals(followeeEmail)) {
            logger.info("Error unfollowing!");
            return null;
        }
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            logger.info("Error unfollowing - user does not exist!");
            return null;
        }
        follower.removeFollowee(followeeEmail);

        final String query = "DELETE FROM following WHERE follower_id = ? AND followee_id = ?;";

        template.update(query, follower.getId(), followee.getId());



        logger.info("{} has unfollowed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }

    @Override
    public List<UserDetailsExtended> getFollowers(String email, Integer limit, String order, Integer sinceId) {
        return null;
    }

    @Override
    public List<UserDetailsExtended> getFollowees(String email, Integer limit, String order, Integer sinceId) {
        return null;
    }

    @Override
    public UserDetailsExtended updateProfile(String email, String name, String about) {
        final String query = "UPDATE user SET name = ?, about = ? WHERE email = ?;";
        final int affectedRows = template.update(query, name, about, email);
        if (affectedRows == 0) {
            logger.info("Error update user profile because user with email {} does not exist!", email);
            return null;
        }
        final User user = getByEmail(email);
        return new UserDetailsExtended(user);
    }

    @Override
    public Long subscribe(long threadId, String email) {
        final User user = getByEmail(email);
        if (user == null) {
            return null;
        }
        final Long userId = user.getId();
        final String query = "INSERT INTO subscription (user_id, thread_id) VALUES (?, ?);";
        try {
            template.update(query, userId, threadId);
        }
        catch (DuplicateKeyException e) {
            logger.info("User {} already subscribed on thread with id={}!", email, threadId);
            return userId;
        }
        return userId;
    }

    @Override
    public Long unsubscribe(long threadId, String email) {
        final User user = getByEmail(email);
        if (user == null) {
            return null;
        }
        final Long userId = user.getId();
        final String query = "DELETE FROM subscription WHERE user_id=? AND thread_id=?;";
            template.update(query, userId, threadId);


        return userId;
    }

    @Override
    public List<PostDetailsExtended> getPosts(String email, LocalDateTime since, Integer limit, String order) {
        if (since == null) {
            if (limit == null) {
                return postDAODataBase.getPostsByUser(email, order);
            }
            else {
                return postDAODataBase.getPostsByUser(email, limit, order);
            }
        }
        else {
            if (limit == null) {
                return postDAODataBase.getPostsByUser(email, since, order);
            }
            else {
                return postDAODataBase.getPostsByUser(email, since, limit, order);
            }
        }
    }



    public List<UserDetailsExtended> getUsersByForum(String forumShortName, String order) {
        final String query = "SELECT u.id, u.username, u.name, u.email, u.about, u.anonymous" +
                " FROM user u JOIN forum f ON u.id = f.user_id" +
                " WHERE f.short_name = ? ORDER BY name " + order + ';';
        return template.query(query, userDetailsExtMapper, forumShortName);
    }


    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, String order) {
        final String query = "SELECT u.id, u.username, u.name, u.email, u.about, u.anonymous" +
                " FROM user u JOIN forum f ON u.id = f.user_id" +
                " WHERE f.short_name = ? AND u.id >= ?  ORDER BY name " + order + ';';
        return template.query(query, userDetailsExtMapper, forumShortName, sinceId);
    }


    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Integer limit, String order) {
        final String query = "SELECT u.id, u.username, u.name, u.email, u.about, u.anonymous" +
                " FROM user u JOIN forum f ON u.id = f.user_id" +
                " WHERE f.short_name = ? ORDER BY name " + order + " LIMIT ?";
        return template.query(query, userDetailsExtMapper, forumShortName, limit);
    }

    @Override
    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, Integer limit, String order) {
        final String query = "SELECT u.id, u.username, u.name, u.email, u.about, u.anonymous" +
                " FROM user u JOIN forum f ON u.id = f.user_id" +
                " WHERE f.short_name = ? AND u.id >= ?  ORDER BY name " + order + " LIMIT ?";
        return template.query(query, userDetailsExtMapper, forumShortName, sinceId, limit);
    }


    RowMapper<User> userMapper = (rs, rowNum) -> {
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


    RowMapper<UserDetailsExtended> userDetailsExtMapper = (rs, i) -> {
        final String username = rs.getString("username");
        final String name = rs.getString("name");
        final String eMail = rs.getString("email");
        final String about = rs.getString("about");
        final long id = rs.getLong("id");
        final boolean anonymous = rs.getBoolean("anonymous");
        final User user = new User(username, name, eMail, about, anonymous);
        user.setId(id);
        return new UserDetailsExtended(user);
    };
}
