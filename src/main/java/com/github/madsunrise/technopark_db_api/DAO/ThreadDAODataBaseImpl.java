package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class ThreadDAODataBaseImpl implements ThreadDAO{
    private final JdbcTemplate template;
    private static final Logger logger = LoggerFactory.getLogger(ThreadDAODataBaseImpl.class.getName());
    public ThreadDAODataBaseImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAODataBaseImpl userDAODataBase;

    @Autowired
    private ForumDAODataBaseImpl forumDAODataBase;


    @Override
    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS thread";
        template.execute(dropTable);
        logger.info("Table thread was dropped");
    }

    @Override
    public void createTable() {
        final String createTable = "CREATE TABLE thread (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(100) NOT NULL," +
                "message TEXT NOT NULL," +
                "slug VARCHAR(100) NOT NULL," +
                "date DATETIME NOT NULL," +
                "user VARCHAR(30) NOT NULL," +
                "forum VARCHAR(30) NOT NULL," +
                "closed TINYINT(1) NOT NULL DEFAULT 0," +
                "deleted TINYINT(1) NOT NULL DEFAULT 0," +
                "likes INT NOT NULL DEFAULT 0," +
                "dislikes INT NOT NULL DEFAULT 0," +
                "posts INT NOT NULL DEFAULT 0," +
                "user_id BIGINT NOT NULL," +
                "forum_id BIGINT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id)," +
                "FOREIGN KEY (forum_id) REFERENCES forum(id)) CHARACTER SET utf8" +
                " DEFAULT COLLATE utf8_general_ci;";
        template.execute(createTable);
        logger.info("Table thread was created");
    }

    @Override
    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM thread;";
        return template.queryForObject(query, Long.class);
    }

    @Override
    public Thread getById(long threadId) {
        try {
            final Thread thread = template.queryForObject(
                    "SELECT * FROM thread WHERE id = ?",
                    (rs, rowNum) -> {
                        final String title = rs.getString("title");
                        final String message = rs.getString("message");
                        final Timestamp timestamp= rs.getTimestamp("date");
                        final LocalDateTime date = timestamp.toLocalDateTime();
                        final String slug = rs.getString("slug");
                        final String user = rs.getString("user");
                        final long userId = rs.getLong("user_id");
                        final String forum = rs.getString("forum");
                        final long forumId = rs.getLong("forum_id");
                        final boolean closed = rs.getBoolean("closed");
                        final boolean deleted = rs.getBoolean("deleted");
                        final int posts = rs.getInt("posts");
                        final int likes = rs.getInt("likes");
                        final int dislikes = rs.getInt("dislikes");
                        final long id = rs.getLong("id");
                        final Thread result = new Thread(title, message, date, slug, user, userId,
                                forum, forumId, closed, deleted);
                        result.setPosts(posts);
                        result.setLikes(likes);
                        result.setDislikes(dislikes);
                        result.setId(id);
                        return result;
                    }, threadId);
            return thread;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    @Override
    public ThreadDetails create(String forumName, String title, boolean closed, String userEmail, LocalDateTime date, String message, String slug, boolean deleted) {
        final Forum forum = forumDAODataBase.getByShortName(forumName);
        if (forum == null) {
            logger.info("Error creating thread because forum \"{}\" does not exist!", forumName);
            return null;
        }
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            logger.info("Error creating thread because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Thread thread = new Thread(title, message, date, slug,
                userEmail, user.getId(), forumName, forum.getId(), closed, deleted);
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new ThreadPstCreator(thread), keyHolder);
        }
        catch (DuplicateKeyException e) {
            logger.info("Error creating thread because it already exists!");
            return null;
        }

        final Map<String, Object> keys = keyHolder.getKeys();
        thread.setId((Long)keys.get("GENERATED_KEY"));
        logger.info("Thread with title={} successful created", title);
        final ThreadDetails<String, String> threadDetails = new ThreadDetails<>(thread);
        threadDetails.setForum(thread.getForum());
        threadDetails.setUser(thread.getUser());
        return threadDetails;
    }

    private static class ThreadPstCreator implements PreparedStatementCreator {
        private final Thread thread;
        ThreadPstCreator(Thread thread) {
            this.thread = thread;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO thread (title, message, date, slug, user, user_id," +
                    "forum, forum_id, closed, deleted) VALUES (?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, thread.getTitle());
            pst.setString(2, thread.getMessage());
            Timestamp date = Timestamp.valueOf(thread.getDate());
            pst.setTimestamp(3, date);
            pst.setString(4, thread.getSlug());
            pst.setString(5, thread.getUser());
            pst.setLong(6, thread.getUserId());
            pst.setString(7, thread.getForum());
            pst.setLong(8, thread.getForumId());
            pst.setBoolean(9, thread.isClosed());
            pst.setBoolean(10, thread.isDeleted());
            return pst;
        }
    }

    @Override
    public void addPost(long threadId) {
        final String query = "UPDATE thread SET posts = posts + 1 WHERE id=?;";
        template.update(query, threadId);
    }

    @Override
    public Long close(long threadId) {
        return null;
    }

    @Override
    public Long open(long threadId) {
        return null;
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId) {
        return this.getDetails(threadId, null);
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId, List<String> related) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error getting thread details - thread with ID={}: does not exist!", threadId);
            return null;
        }
        final ThreadDetailsExtended result = new ThreadDetailsExtended(thread);

        if (related != null && related.contains("user")) {
            final User user = userDAODataBase.getByEmail(thread.getUser());
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            result.setUser(userDetails);
        }
        else {
            result.setUser(thread.getUser());
        }

        if (related != null && related.contains("forum")) {
            final Forum forum = forumDAODataBase.getByShortName(thread.getForum());
            final ForumDetails<String> forumDetails = new ForumDetails<>(forum);
            forumDetails.setUser(forum.getUser());
            result.setForum(forumDetails);
        }
        else {
            result.setForum(thread.getForum());
        }

        logger.info("Getting thread (ID={}) details is success", threadId);
        return result;
    }

    @Override
    public long save(Thread thread) {
        return 0;
    }

    @Override
    public Long remove(long threadId) {
        return null;
    }

    @Override
    public Long restore(long threadId) {
        return null;
    }

    @Override
    public Long subscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error subscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = userDAODataBase.subscribe(threadId, userEmail);
        if (userId == null) {
            logger.info("Error subscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        logger.info("User {} has subscribed to thread with ID={}", userEmail, threadId);
        return thread.getId();
    }

    @Override
    public Long unsubscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error unsubscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = userDAODataBase.unsubscribe(threadId, userEmail);
        if (userId == null) {
            logger.info("Error unsubscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        logger.info("User {} has unsubscribed from thread with ID={}", userEmail, threadId);
        return thread.getId();
    }

    @Override
    public List<PostDetailsExtended> getPosts(long threadId, LocalDateTime since, Integer limit, String order, String sort) {
        return null;
    }

    @Override
    public ThreadDetailsExtended vote(long threadId, int vote) {
        return null;
    }

    @Override
    public ThreadDetailsExtended update(long threadId, String message, String slug) {
        return null;
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since, Integer limit, String order, List<String> related) {
        return null;
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, LocalDateTime since, Integer limit, String order) {
        return null;
    }
}
