package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Post;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

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

    @Autowired
    private PostDAODataBaseImpl postDAODataBase;


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
    public ThreadDetails create(String forumName, String title, boolean closed,
                                String userEmail, LocalDateTime date, String message, String slug, boolean deleted) {
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


    public void removePost(long threadId) {
        final String query = "UPDATE thread SET posts = posts - 1 WHERE id=?;";
        template.update(query, threadId);
    }


    @Override
    public boolean close(long threadId) {
        final String query = "UPDATE thread SET closed=? WHERE id=?;";
        final int affectedRows = template.update(query, 1, threadId);
        if (affectedRows == 0) {
            logger.info("Closing thread with ID={} failed", threadId);
            return false;
        }
        logger.info("Closed thread with ID={}", threadId);
        return true;
    }

    @Override
    public boolean open(long threadId) {
        final String query = "UPDATE thread SET closed=? WHERE id=?;";
        final int affectedRows = template.update(query, 0, threadId);
        if (affectedRows == 0) {
            logger.info("Opening thread with ID={} failed", threadId);
            return false;
        }
        logger.info("Opened thread with ID={}", threadId);
        return true;
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
    public boolean remove(long threadId) {
        final String query = "UPDATE thread SET deleted=? WHERE id=?;";
        final int affectedRows = template.update(query, true, threadId);
        if (affectedRows == 0) {
            logger.info("Removing thread with ID={} failed", threadId);
            return false;
        }

        postDAODataBase.markDeleted(threadId);
        logger.info("Removed thread with ID={}", threadId);
        return true;
    }

    @Override
    public boolean restore(long threadId){
        final String query = "UPDATE thread SET deleted=? WHERE id=?;";
        final int affectedRows = template.update(query, false, threadId);
        if (affectedRows == 0) {
            logger.info("Restoring thread with ID={} failed", threadId);
            return false;
        }
        postDAODataBase.markRestored(threadId);
        logger.info("Restored thread with ID={}", threadId);
        return true;
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
    public List<PostDetailsExtended> getPosts(long threadId, LocalDateTime since,
                                              Integer limit, String order, String sort) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error getting post list because thread with ID={} does not exist!", threadId);
            return null;
        }

        final List<PostDetailsExtended> posts;
        if (since == null) {
            if (limit != null && sort.equals("flat")) {
                posts = postDAODataBase.getPostsByThread(threadId, limit, order);
            }
            else {
                posts = postDAODataBase.getPostsByThread(threadId, order);
            }
        }
        else {
            if (limit != null && sort.equals("flat")) {
                posts = postDAODataBase.getPostsByThread(threadId, since, limit, order);
            }
            else {
                posts = postDAODataBase.getPostsByThread(threadId, since, order);
            }
        }

        if (sort.equals("flat")) {
            return posts;
        }

        // Add sort here
            if (order.equals("asc")) {
                Collections.sort(posts, new PathComparatorAsc());
            }
            else {
                Collections.sort(posts, new PathComparatorDesc());
            }


        if (limit != null && limit < posts.size()) {
            if (sort.equals("parent_tree")) {
                int rootCount = 0;
                int postsCount = 0;
                for (PostDetailsExtended postDetails: posts) {
                    final String path = postDetails.getPath();
                    if (!path.contains(".")) {  // Перед нами корневой пост
                        rootCount++;
                        if (rootCount > limit) {
                            break;
                        }
                    }
                    postsCount++;
                }
                return posts.subList(0, postsCount);
            }
            return posts.subList(0, limit); // для tree
        }

        return posts;
    }

    @Override
    public ThreadDetailsExtended vote(long threadId, int vote) {
        final String query;
        if (vote == 1) {
            query = "UPDATE thread SET likes = likes + 1 WHERE id=?;";
        }
        else {
            query = "UPDATE thread SET dislikes = dislikes + 1 WHERE id=?;";
        }
        final int affectedRows = template.update(query, threadId);
        if (affectedRows == 0) {
            logger.info("Error vote because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Thread thread = getById(threadId);
        return new ThreadDetailsExtended(thread);
    }

    @Override
    public ThreadDetailsExtended update(long threadId, String message, String slug) {
        final String query = "UPDATE thread SET message = ?, slug = ? WHERE id = ?;";
        final int affectedRows = template.update(query, message, slug, threadId);
        if (affectedRows == 0) {
            logger.info("Error update thread because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Thread thread = getById(threadId);
        return new ThreadDetailsExtended(thread);

    }



    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, String order, List<String> related) {
        final String query = "SELECT * FROM thread WHERE forum=? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, forumShortName);
        return makeDetailsWithRelations(threads, related);
    }


    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName,
                                                         LocalDateTime since, String order, List<String> related) {
        final String query = "SELECT * FROM thread WHERE forum=? AND date >= ? ORDER BY date " + order + ';';
        final List<Thread> threads = template.query(query, threadMapper, forumShortName, since);
        return makeDetailsWithRelations(threads, related);
    }


    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName,
                                                         Integer limit, String order, List<String> related) {
        final String query = "SELECT * FROM thread WHERE forum=? ORDER BY date " + order + " LIMIT ?;";
        final List<Thread> threads = template.query(query, threadMapper, forumShortName, limit);
        return makeDetailsWithRelations(threads, related);
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since,
                                                         Integer limit, String order, List<String> related) {
        final String query = "SELECT * FROM thread WHERE forum=? AND date >= ? ORDER BY date " + order + " LIMIT ?;";
        final List<Thread> threads = template.query(query, threadMapper, forumShortName, since, limit);
        return makeDetailsWithRelations(threads, related);
    }

    private List<ThreadDetailsExtended> makeDetailsWithRelations (Iterable<Thread> threads, Collection<String> related) {
        final List<ThreadDetailsExtended> result = new ArrayList<>();
        for (Thread thread: threads) {
            final ThreadDetailsExtended details = new ThreadDetailsExtended(thread);
            if (related != null && related.contains("forum")) {
                final ForumDetails forumDetails = forumDAODataBase.getDetails(thread.getForum(), null);
                details.setForum(forumDetails);
            } else {
                details.setForum(thread.getForum());
            }

            if (related != null && related.contains("user")) {
                final UserDetailsExtended userDetails = userDAODataBase.getDetails(thread.getUser());
                details.setUser(userDetails);
            } else {
                details.setUser(thread.getUser());
            }
            result.add(details);
        }
        return result;
    }










    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, LocalDateTime since, Integer limit, String order) {
        return null;
    }



    static class DateComparator implements Comparator<ThreadDetailsExtended> {
        @Override
        public int compare(ThreadDetailsExtended t1, ThreadDetailsExtended t2) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime d1 = LocalDateTime.parse(t1.getDate(), formatter);
            final LocalDateTime d2 = LocalDateTime.parse(t2.getDate(), formatter);
            return d1.compareTo(d2);
        }
    }

    static class PathComparatorAsc implements Comparator<PostDetailsExtended> {
        @Override
        public int compare(PostDetailsExtended p1, PostDetailsExtended p2) {
            final String path1 = p1.getPath();
            final String path2 = p2.getPath();
            return path1.compareTo(path2);
        }
    }

    static class PathComparatorDesc implements Comparator<PostDetailsExtended> {
        @Override
        public int compare(PostDetailsExtended p1, PostDetailsExtended p2) {
            final String path1 = p1.getPath();
            final String path2 = p2.getPath();
            if (path1.contains(".") || path2.contains(".")) {
                final String[] array1 = path1.split("\\.");
                final String[] array2 = path2.split("\\.");
                if (array1[0].equals(array2[0])) {
                    return path1.compareTo(path2);
                } else {
                    return path2.compareTo(path1);
                }
            }

            return path2.compareTo(path1);
        }
    }



    static class DatePostsComparator implements Comparator<PostDetailsExtended> {
        @Override
        public int compare(PostDetailsExtended p1, PostDetailsExtended p2) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime d1 = LocalDateTime.parse(p1.getDate(), formatter);
            final LocalDateTime d2 = LocalDateTime.parse(p2.getDate(), formatter);
            return d1.compareTo(d2);
        }
    }


    RowMapper<Thread> threadMapper = (rs, i) -> {
        final String title = rs.getString("title");
        final String message = rs.getString("message");
        final Timestamp timestamp = rs.getTimestamp("date");
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
    };
}
