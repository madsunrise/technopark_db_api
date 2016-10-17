package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        return 0;
    }

    @Override
    public User getById(Long id) {
        return null;
    }

    @Override
    public User getByEmail(String email) {
        return null;
    }

    @Override
    public UserDetails create(String username, String name, String email, String about, boolean anonymous) {
        return null;
    }

    @Override
    public UserDetailsExtended getDetails(String email) {
        return null;
    }

    @Override
    public UserDetailsExtended follow(String followerEmail, String followeeEmail) {
        return null;
    }

    @Override
    public UserDetailsExtended unfollow(String followerEmail, String followeeEmail) {
        return null;
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
        return null;
    }

    @Override
    public Long subscribe(long threadId, String email) {
        return null;
    }

    @Override
    public Long unsubscribe(long threadId, String email) {
        return null;
    }

    @Override
    public List<PostDetailsExtended> getPosts(String email, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, Integer limit, String order) {
        return null;
    }
}
