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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class ForumDAO {

    private final JdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumDAO.class.getName());
    public ForumDAO(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAO userDAODataBase;
    @Autowired
    private ThreadDAO threadDAODataBase;
    @Autowired
    private PostDAO postDAODataBase;



    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS forum";
        template.execute(dropTable);
        LOGGER.info("Table forum was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE forum (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) UNIQUE NOT NULL," +
                "short_name VARCHAR(30) UNIQUE NOT NULL," +
                "user_id BIGINT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id)) CHARACTER SET utf8" +
                " DEFAULT COLLATE utf8_general_ci;";
        template.execute(createTable);
        LOGGER.info("Table forum was created");
    }



    public void fillUserForumTable() {
        final String getAllForums = "SELECT * FROM forum";
        final List<Forum> forums = template.query(getAllForums, (rs, rowNum) -> {
            final String name = rs.getString("name");
            final String shortName = rs.getString("short_name");
            final Long userId = rs.getLong("user_id");
            final long id = rs.getLong("id");
            final Forum result = new Forum(name, shortName, userId);
            result.setId(id);
            return result;
        });

        final String query = "INSERT INTO user_forum(forum_id, user_id) VALUES (?,?);";
        for (Forum forum: forums) {
            final List<UserDetailsExtended> users = getUsers(forum.getShortName(), null, null, "asc");
            for (UserDetailsExtended user: users) {
                template.update(query, forum.getId(), user.getId());
                LOGGER.info("Processing...");
            }
        }
        LOGGER.info("FINISHED!");
    }



    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM forum;";
        return template.queryForObject(query, Long.class);
    }



    public Forum getById (long forumId) {
        try {
            return template.queryForObject(
                    "SELECT * FROM forum WHERE id = ?",
                    (rs, rowNum) -> {
                        final String name = rs.getString("name");
                        final String shortName = rs.getString("short_name");
                        final Long userId = rs.getLong("user_id");
                        final Forum result = new Forum(name, shortName, userId);
                        result.setId(forumId);
                        return result;
                    }, forumId);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }




    public Forum getByShortName(String shortName) {
        try {
            return template.queryForObject(
                    "SELECT * FROM forum WHERE short_name = ?",
                    (rs, rowNum) -> {
                        final String name = rs.getString("name");
                        final Long userId = rs.getLong("user_id");
                        final long id = rs.getLong("id");
                        final Forum result = new Forum(name, shortName, userId);
                        result.setId(id);
                        return result;
                    }, shortName);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public ForumDetails create(String name, String shortName, String userEmail) {
   //     final long start = System.currentTimeMillis();
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error creating forum because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Forum forum = new Forum(name, shortName, user.getId());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new ForumPstCreator(forum), keyHolder);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum \"{}\" - it already exists!", shortName);
            return null;
        }
        final Map<String, Object> keys = keyHolder.getKeys();
        forum.setId((Long)keys.get("GENERATED_KEY"));
 //       final long end = System.currentTimeMillis();
//        LOGGER.info("Forum \"{}\" successful created, time: {}", shortName, end-start);
        return new ForumDetails(forum);
    }




    public ForumDetails getDetails(long forumId, Collection<String> related) {
        final Forum forum = getById(forumId);
        if (forum == null) {
            LOGGER.info("Error getting forum details because forum with ID=\"{}\": does not exist!", forumId);
            return null;
        }

     //   LOGGER.info("Getting forum details with ID=\"{}\" is success", forumId);
        final User user = userDAODataBase.getById(forum.getUserId());
        if (related != null && related.contains("user")) {
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), userDetails);
        }
        return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), user.getEmail());
    }



    public ForumDetails getDetails(String shortName) {
        return  this.getDetails(shortName, null);
    }


    public ForumDetails getDetails(String shortName, List<String> related) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            LOGGER.info("Error getting forum details because forum \"{}\": does not exist!", shortName);
            return null;
        }

       // LOGGER.info("Getting forum details \"{}\" is success", shortName);
        final User user = userDAODataBase.getById(forum.getUserId());
        if (related != null && related.contains("user")) {
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), userDetails);
        }
        return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), user.getEmail());
    }




    public List<PostDetailsExtended> getPosts(String shortName, LocalDateTime since,
                                              Integer limit, String order, List<String> related) {
        if (since == null) {
            if (limit == null) {
                return postDAODataBase.getPostsByForum(shortName, order, related);
            }
            else {
                return postDAODataBase.getPostsByForum(shortName, limit, order, related);
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


    public List<ThreadDetailsExtended> getThreads(String shortName, LocalDateTime since,
                                                  Integer limit, String order, List<String> related) {
        if (since == null) {
            if (limit == null) {
                return threadDAODataBase.getThreadsByForum(shortName, order, related);
            }
            else {
                return threadDAODataBase.getThreadsByForum(shortName, limit, order, related);
            }
        }
        else {
            if (limit == null) {
                return threadDAODataBase.getThreadsByForum(shortName, since, order, related);
            }
            else {
                return threadDAODataBase.getThreadsByForum(shortName, since, limit, order,related);
            }
        }
    }


    public List<UserDetailsExtended> getUsers(String shortName, Long sinceId, Integer limit, String order) {
        if (sinceId == null) {
            if (limit == null) {
                return userDAODataBase.getUsersByForum(shortName, order);
            }
            else {
                return userDAODataBase.getUsersByForum(shortName, limit, order);
            }
        }
        else {
            if (limit == null) {
                return userDAODataBase.getUsersByForum(shortName, sinceId, order);
            }
            else {
                return userDAODataBase.getUsersByForum(shortName, sinceId, limit, order);
            }
        }
    }






    private static class ForumPstCreator implements PreparedStatementCreator {
        private final Forum forum;
        ForumPstCreator(Forum forum) {
            this.forum = forum;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO forum (name, short_name, user_id) VALUES (?,?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, forum.getName());
            pst.setString(2, forum.getShortName());
            pst.setLong(3, forum.getUserId());
            return pst;
        }
    }
}
