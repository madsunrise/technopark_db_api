package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.*;
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
public class ThreadDAO {
    private final JdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDAO.class.getName());
    public ThreadDAO(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAO userDAODataBase;

    @Autowired
    private ForumDAO forumDAODataBase;

    @Autowired
    private PostDAO postDAODataBase;



    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS thread";
        template.execute(dropTable);
        LOGGER.info("Table thread was dropped");
    }


    public void createTable() {
        final String createTable = "CREATE TABLE thread (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(100) NOT NULL," +
                "message TEXT NOT NULL," +
                "slug VARCHAR(100) NOT NULL," +
                "date DATETIME NOT NULL," +
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
        LOGGER.info("Table thread was created");
    }


    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM thread;";
        return template.queryForObject(query, Long.class);
    }


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
                        final long userId = rs.getLong("user_id");
                        final long forumId = rs.getLong("forum_id");
                        final boolean closed = rs.getBoolean("closed");
                        final boolean deleted = rs.getBoolean("deleted");
                        final int posts = rs.getInt("posts");
                        final int likes = rs.getInt("likes");
                        final int dislikes = rs.getInt("dislikes");
                        final long id = rs.getLong("id");
                        final Thread result = new Thread(title, message, date, slug, userId, forumId, closed, deleted);
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


    public ThreadDetails create(String forumName, String title, boolean closed,
                                String userEmail, LocalDateTime date, String message, String slug, boolean deleted) {
        final long start = System.currentTimeMillis();
        final Forum forum = forumDAODataBase.getByShortName(forumName);
        if (forum == null) {
            LOGGER.info("Error creating thread because forum \"{}\" does not exist!", forumName);
            return null;
        }
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error creating thread because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Thread thread = new Thread(title, message, date, slug, user.getId(), forum.getId(), closed, deleted);
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new ThreadPstCreator(thread), keyHolder);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating thread because it already exists!");
            return null;
        }

        final Map<String, Object> keys = keyHolder.getKeys();
        thread.setId((Long)keys.get("GENERATED_KEY"));

        final ThreadDetails<String, String> threadDetails = new ThreadDetails<>(thread);
        threadDetails.setForum(forumName);
        threadDetails.setUser(userEmail);

        final long end = System.currentTimeMillis();
        LOGGER.info("Thread with title={} successful created, time: {}", title, end-start);
        return threadDetails;
    }

    private static class ThreadPstCreator implements PreparedStatementCreator {
        private final Thread thread;
        ThreadPstCreator(Thread thread) {
            this.thread = thread;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO thread (title, message, date, slug, user_id, " +
                    "forum_id, closed, deleted) VALUES (?,?,?,?,?,?,?,?);";
            PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, thread.getTitle());
            pst.setString(2, thread.getMessage());
            final Timestamp date = Timestamp.valueOf(thread.getDate());
            pst.setTimestamp(3, date);
            pst.setString(4, thread.getSlug());
            pst.setLong(5, thread.getUserId());
            pst.setLong(6, thread.getForumId());
            pst.setBoolean(7, thread.isClosed());
            pst.setBoolean(8, thread.isDeleted());
            return pst;
        }
    }


    public void addPost(long threadId) {
        final String query = "UPDATE thread SET posts = posts + 1 WHERE id = ?;";
        template.update(query, threadId);
    }


    public void removePost(long threadId) {
        final String query = "UPDATE thread SET posts = posts - 1 WHERE id = ?;";
        template.update(query, threadId);
    }



    public boolean close(long threadId) {
        final String query = "UPDATE thread SET closed=? WHERE id=?;";
        final int affectedRows = template.update(query, 1, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Closing thread with ID={} failed", threadId);
            return false;
        }
        LOGGER.info("Closed thread with ID={}", threadId);
        return true;
    }


    public boolean open(long threadId) {
        final String query = "UPDATE thread SET closed=? WHERE id=?;";
        final int affectedRows = template.update(query, 0, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Opening thread with ID={} failed", threadId);
            return false;
        }
        LOGGER.info("Opened thread with ID={}", threadId);
        return true;
    }


    public ThreadDetailsExtended getDetails(long threadId) {
        return this.getDetails(threadId, null);
    }


    public ThreadDetailsExtended getDetails(long threadId, List<String> related) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            LOGGER.info("Error getting thread details - thread with ID={}: does not exist!", threadId);
            return null;
        }

        LOGGER.info("Getting thread (ID={}) details is success", threadId);
        return threadToThreadDetails(thread, related);
    }



    public boolean remove(long threadId) {
        final String query = "UPDATE thread SET deleted=? WHERE id=?;";
        final int affectedRows = template.update(query, true, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Removing thread with ID={} failed", threadId);
            return false;
        }

        postDAODataBase.markDeleted(threadId);
        LOGGER.info("Removed thread with ID={}", threadId);
        return true;
    }


    public boolean restore(long threadId){
        final String query = "UPDATE thread SET deleted=? WHERE id=?;";
        final int affectedRows = template.update(query, false, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Restoring thread with ID={} failed", threadId);
            return false;
        }
        postDAODataBase.markRestored(threadId);
        LOGGER.info("Restored thread with ID={}", threadId);
        return true;
    }




    public Long subscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            LOGGER.info("Error subscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = userDAODataBase.subscribe(threadId, userEmail);
        if (userId == null) {
            LOGGER.info("Error subscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        LOGGER.info("User {} has subscribed to thread with ID={}", userEmail, threadId);
        return thread.getId();
    }


    public Long unsubscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            LOGGER.info("Error unsubscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = userDAODataBase.unsubscribe(threadId, userEmail);
        if (userId == null) {
            LOGGER.info("Error unsubscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        LOGGER.info("User {} has unsubscribed from thread with ID={}", userEmail, threadId);
        return thread.getId();
    }





    public List<PostDetailsExtended> getPosts(long threadId, LocalDateTime since,
                                              Integer limit, String order, String sort) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            LOGGER.info("Error getting post list because thread with ID={} does not exist!", threadId);
            return null;
        }

        return postDAODataBase.getPostsByThread(threadId, since, sort, order, limit);
    }



    public ThreadDetailsExtended vote(long threadId, int vote) {
        final String query;
        if (vote == 1) {
            query = "UPDATE thread SET likes = likes + 1 WHERE id = ?;";
        }
        else {
            query = "UPDATE thread SET dislikes = dislikes + 1 WHERE id = ?;";
        }
        final int affectedRows = template.update(query, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Error vote because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Thread thread = getById(threadId);
        return threadToThreadDetails(thread, null);
    }


    public ThreadDetailsExtended update(long threadId, String message, String slug) {
        final String query = "UPDATE thread SET message = ?, slug = ? WHERE id = ?;";
        final int affectedRows = template.update(query, message, slug, threadId);
        if (affectedRows == 0) {
            LOGGER.info("Error update thread because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Thread thread = getById(threadId);
        return threadToThreadDetails(thread, null);

    }



    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, String order, List<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting threads by forum because forum {} does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE forum_id = ? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, forum.getId());

        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, related));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName,
                                                         LocalDateTime since, String order, List<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting threads by forum because forum {} does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE forum_id = ? AND date >= ? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, forum.getId(), since);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, related));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName,
                                                         Integer limit, String order, List<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting threads by forum because forum {} does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE forum_id = ? ORDER BY date " + order + " LIMIT ?;";
        final List<Thread> threads = template.query(query, threadMapper, forum.getId(), limit);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, related));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since,
                                                         Integer limit, String order, List<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting threads by forum because forum {} does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE forum_id = ? AND date >= ?" +
                " ORDER BY date " + order + " LIMIT ?;";

        final List<Thread> threads = template.query(query, threadMapper, forum.getId(), since, limit);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, related));
        }
        return threadsDetails;
    }





    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting threads by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE user_id = ? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, user.getId());
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, null));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, LocalDateTime since, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting threads by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE user_id = ? AND date >= ? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, user.getId(), since);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, null));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, Integer limit, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting threads by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE user_id = ? ORDER BY date " + order + " LIMIT ?;";
        final List<Thread> threads = template.query(query, threadMapper, user.getId(), limit);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, null));
        }
        return threadsDetails;
    }


    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail,
                                                        LocalDateTime since, Integer limit, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting threads by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM thread WHERE user_id = ? AND date >= ? ORDER BY date " + order + " LIMIT ?;";
        final List<Thread> threads = template.query(query, threadMapper, user.getId(), since, limit);
        final List<ThreadDetailsExtended> threadsDetails = new ArrayList<>();
        for (Thread thread: threads) {
            threadsDetails.add(threadToThreadDetails(thread, null));
        }
        return threadsDetails;
    }





    private ThreadDetailsExtended threadToThreadDetails (Thread thread, Collection<String> related) {
        final ThreadDetailsExtended result = new ThreadDetailsExtended(thread);
        final Forum forum = forumDAODataBase.getById(thread.getForumId());
        final User user = userDAODataBase.getById(thread.getUserId());

        if (related != null && related.contains("forum")) {
            final ForumDetails<String> forumDetails = new ForumDetails<>(forum);
            final User forumUser = userDAODataBase.getById(forum.getUserId());
            forumDetails.setUser(forumUser.getEmail());
            result.setForum(forumDetails);
        }
        else {
            result.setForum(forum.getShortName());
        }

        if (related != null && related.contains("user")) {
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            result.setUser(userDetails);
        }
        else {
            result.setUser(user.getEmail());
        }
        return result;
    }


    RowMapper<Thread> threadMapper = (rs, i) -> {
        final String title = rs.getString("title");
        final String message = rs.getString("message");
        final Timestamp timestamp = rs.getTimestamp("date");
        final LocalDateTime date = timestamp.toLocalDateTime();
        final String slug = rs.getString("slug");
        final long userId = rs.getLong("user_id");
        final long forumId = rs.getLong("forum_id");
        final boolean closed = rs.getBoolean("closed");
        final boolean deleted = rs.getBoolean("deleted");
        final int posts = rs.getInt("posts");
        final int likes = rs.getInt("likes");
        final int dislikes = rs.getInt("dislikes");
        final long id = rs.getLong("id");
        final Thread result = new Thread(title, message, date, slug, userId, forumId, closed, deleted);
        result.setPosts(posts);
        result.setLikes(likes);
        result.setDislikes(dislikes);
        result.setId(id);
        return result;
    };
}
