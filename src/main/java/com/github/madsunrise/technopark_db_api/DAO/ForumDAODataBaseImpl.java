package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class ForumDAODataBaseImpl implements ForumDAO {

    private final JdbcTemplate template;
    private static final Logger logger = LoggerFactory.getLogger(ForumDAODataBaseImpl.class.getName());
    public ForumDAODataBaseImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAODataBaseImpl userDAODataBase;
    @Autowired
    private ThreadDAODataBaseImpl threadDAODataBase;
    @Autowired
    private PostDAODataBaseImpl postDAODataBase;


    @Override
    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS forum";
        template.execute(dropTable);
        logger.info("Table forum was dropped");
    }
    @Override
    public void createTable() {
        final String createTable = "CREATE TABLE forum (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) UNIQUE NOT NULL," +
                "short_name VARCHAR(30) UNIQUE NOT NULL," +
                "user VARCHAR(30) NOT NULL," +
                "user_id BIGINT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id)) CHARACTER SET utf8" +
                " DEFAULT COLLATE utf8_general_ci;";
        template.execute(createTable);
        logger.info("Table forum was created");
    }

    @Override
    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM forum;";
        return template.queryForObject(query, Long.class);
    }


    @Override
    public Forum getByShortName(String shortName) {
        try {
            final Forum forum = template.queryForObject(
                    "SELECT * FROM forum WHERE short_name = ?",
                    (rs, rowNum) -> {
                        final String name = rs.getString("name");
                        final String user = rs.getString("user");
                        final Long userId = rs.getLong("user_id");
                        final long id = rs.getLong("id");
                        final Forum result = new Forum(name, shortName, user, userId);
                        result.setId(id);
                        return result;
                    }, shortName);
            return forum;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public ForumDetails create(String name, String shortName, String userEmail) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            logger.info("Error creating forum because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Forum forum = new Forum(name, shortName, userEmail, user.getId());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new ForumPstCreator(forum), keyHolder);
        }
        catch (DuplicateKeyException e) {
            logger.info("Error creating forum \"{}\" - it already exists!", shortName);
            return null;
        }
        final Map<String, Object> keys = keyHolder.getKeys();
        forum.setId((Long)keys.get("GENERATED_KEY"));
        logger.info("Forum \"{}\" successful created", shortName);
        return new ForumDetails(forum);
    }

    private static class ForumPstCreator implements PreparedStatementCreator {
        private final Forum forum;
        ForumPstCreator(Forum forum) {
            this.forum = forum;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO forum (name, short_name, user, user_id) VALUES (?,?,?,?);";
            PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, forum.getName());
            pst.setString(2, forum.getShortName());
            pst.setString(3, forum.getUser());
            pst.setLong(4, forum.getUserId());
            return pst;
        }
    }



    @Override
    public ForumDetails getDetails(String shortName) {
        return  this.getDetails(shortName, null);
    }

    @Override
    public ForumDetails getDetails(String shortName, List<String> related) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            logger.info("Error getting forum details because forum \"{}\": does not exist!", shortName);
            return null;
        }

        logger.info("Getting forum details \"{}\" is success", shortName);
        if (related != null && related.contains("user")) {
            final User user = userDAODataBase.getByEmail(forum.getUser());
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), userDetails);
        }
        return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), forum.getUser());
    }



    @Override
    public List<PostDetailsExtended> getPosts(String shortName, LocalDateTime since,
                                              Integer limit, String order, List<String> related) {
        if (since == null) {
            if (limit == null) {
                return postDAODataBase.getPostsByForum(shortName, order, related);
            }
            else {
                return postDAODataBase.getPostsByForum(shortName, since, order, related);
            }
        }
        else {
            if (limit == null) {
                return postDAODataBase.getPostsByForum(shortName, since, order, related);
            }
            else {
                return postDAODataBase.getPostsByForum(shortName, since, limit, order,related);
            }
        }
    }

    @Override
    public List<ThreadDetailsExtended> getThreads(String shortName, LocalDateTime since, Integer limit, String order, List<String> related) {
        return null;
    }

    @Override
    public List<UserDetailsExtended> getUsers(String shortName, Long sinceId, Integer limit, String order) {
        return null;
    }
}
