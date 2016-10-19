package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Post;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class PostDAOImpl implements PostDAO {

    private final JdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostDAOImpl.class.getName());
    public PostDAOImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAOImpl userDAODataBase;
    @Autowired
    private ForumDAOImpl forumDAODataBase;
    @Autowired
    private ThreadDAOImpl threadDAODataBase;

    @Override
    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS post";
        template.execute(dropTable);
        LOGGER.info("Table post was dropped");
    }

    @Override
    public void createTable() {
        final String createTable = "CREATE TABLE post (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "message TEXT NOT NULL," +
                "date DATETIME NOT NULL," +
                "path VARCHAR(255) NOT NULL," +
                "parent BIGINT," +
                "approved TINYINT(1) NOT NULL DEFAULT 0," +
                "highlighted TINYINT(1) NOT NULL DEFAULT 0," +
                "edited TINYINT(1) NOT NULL DEFAULT 0," +
                "spam TINYINT(1) NOT NULL DEFAULT 0," +
                "deleted TINYINT(1) NOT NULL DEFAULT 0," +
                "likes INT NOT NULL DEFAULT 0," +
                "dislikes INT NOT NULL DEFAULT 0," +
                "user_id BIGINT NOT NULL," +
                "forum_id BIGINT NOT NULL," +
                "thread_id BIGINT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id)," +
                "FOREIGN KEY (forum_id) REFERENCES forum(id)," +
                "FOREIGN KEY (thread_id) REFERENCES thread(id)) CHARACTER SET utf8" +
                " DEFAULT COLLATE utf8_general_ci;";
        template.execute(createTable);
        LOGGER.info("Table post was created");
    }

    @Override
    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM post;";
        return template.queryForObject(query, Long.class);
    }

    @Override
    public Post getById(long postId) {
        try {
            return template.queryForObject(
                    "SELECT * FROM post WHERE id = ?",
                    postMapper, postId);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    @Override
    public PostDetails create(LocalDateTime date, long threadId, String message, String userEmail,
                              String forumShortName, Long parent, boolean approved,
                              boolean highlighted, boolean edited, boolean spam, boolean deleted) {

        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error creating post because forum \"{}\" does not exist!", forumShortName);
            return null;
        }
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error creating post because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Post post = new Post(message, date, threadId, user.getId(), forum.getId(),
                parent, approved, highlighted, edited, spam, deleted);
        post.setPath(generatePath(parent));

        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new PostPstCreator(post), keyHolder);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating post because it already exists!");
            return null;
        }

        final Map<String, Object> keys = keyHolder.getKeys();
        post.setId((Long)keys.get("GENERATED_KEY"));

        LOGGER.info("Post with id={} successful created", post.getId());
        threadDAODataBase.addPost(threadId);

        final PostDetails<String, String, Long> postDetails = new PostDetails<>(post);
        postDetails.setForum(forumShortName);
        postDetails.setUser(userEmail);
        postDetails.setThread(threadId);
        return postDetails;
    }

    private static class PostPstCreator implements PreparedStatementCreator {
        private final Post post;
        PostPstCreator(Post post) {
            this.post = post;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO post (message, date, thread_id, user_id, forum_id," +
                    "path, parent, approved, highlighted, edited, spam, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, post.getMessage());
            final Timestamp date = Timestamp.valueOf(post.getDate());
            pst.setTimestamp(2, date);
            pst.setLong(3, post.getThreadId());
            pst.setLong(4, post.getUserId());
            pst.setLong(5, post.getForumId());
            pst.setString(6, post.getPath());
            final Long parent = post.getParent();
            if (parent != null) {
                pst.setLong(7, post.getParent());
            }
            else {
                pst.setNull(7, Types.BIGINT);
            }
            pst.setBoolean(8, post.isApproved());
            pst.setBoolean(9, post.isHighlighted());
            pst.setBoolean(10, post.isEdited());
            pst.setBoolean(11, post.isSpam());
            pst.setBoolean(12, post.isDeleted());
            return pst;
        }
    }

    private String generatePath (Long parentId) {
        final StringBuilder result = new StringBuilder();
        if (parentId != null) {
            final Post post = getById(parentId);
            final String parentPath = post.getPath();
            result.append(parentPath);
            result.append('.');
        }

        final int index = getMaxChildIndex(parentId) + 1;
        String indexStr = Integer.toString(index);
        switch (indexStr.length()) {
            case 1: indexStr = "000" + indexStr;
                break;
            case 2: indexStr = "00" + indexStr;
                break;
            case 3: indexStr = '0' + indexStr;
                break;
            default: break;
        }

        result.append(indexStr);
        return result.toString();
    }


    private int getMaxChildIndex (Long parentId) {
        final List<String> paths;
        if (parentId == null) {
            final String query = "SELECT path FROM post WHERE parent is NULL;";
            paths = template.queryForList(query, String.class);
        }
        else {
            final String query = "SELECT path FROM post WHERE parent = ?;";
            paths = template.queryForList(query, String.class, parentId);
        }

        int max = 0;
        if (paths.isEmpty()) {
            return max;
        }

        if (paths.get(0).contains(".")) { // пути с точкой
            for (String path: paths) {
                final String[] parts = path.split("\\.");
                final int currentIndex = Integer.parseInt(parts[parts.length - 1]);
                if (currentIndex > max) {
                    max = currentIndex;
                }
            }
        }
        else {
            for (String path: paths) {
                final int currentIndex = Integer.parseInt(path);
                if (currentIndex > max) {
                    max = currentIndex;
                }
            }
        }
        return max;
    }


    @Override
    public PostDetailsExtended getDetails(long id) {
        return this.getDetails(id, null);
    }

    @Override
    public PostDetailsExtended getDetails(long id, List<String> related) {
        final Post post = getById(id);
        if (post == null) {
            LOGGER.info("Error getting post details - post with ID={}: does not exist!", id);
            return null;
        }
        LOGGER.info("Getting post (ID={}) details is success", id);
        return postToPostDetails (post, related);
    }





    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName, LocalDateTime since,
                                                     Integer limit, String order, Collection<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting posts by forum {} because it does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM post WHERE forum_id = ? AND date >= ? " +
                "ORDER BY date " + order + " LIMIT ?;";
        final Timestamp sinceTsmp = Timestamp.valueOf(since);
        final List<Post> posts = template.query(query, postMapper,
                forum.getId(), sinceTsmp, limit);

        return listPostToPostDetails(posts, related);
    }

    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName, Integer limit,
                                                     String order, Collection<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting posts by forum {} because it does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM post WHERE forum_id = ? ORDER BY date " + order + " LIMIT ?;";
        final List<Post> posts = template.query(query, postMapper,
                forum.getId(), limit);

        return listPostToPostDetails(posts, related);
    }

    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName, LocalDateTime since,
                                                     String order, Collection<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting posts by forum {} because it does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM post WHERE forum_id = ? AND date >= ? ORDER BY date " + order + ';';
        final Timestamp sinceTsmp = Timestamp.valueOf(since);
        final List<Post> posts = template.query(query, postMapper,
                forum.getId(), sinceTsmp);

        return listPostToPostDetails(posts, related);
    }

    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName,
                                                     String order, Collection<String> related) {
        final Forum forum = forumDAODataBase.getByShortName(forumShortName);
        if (forum == null) {
            LOGGER.info("Error getting posts by forum {} because it does not exist", forumShortName);
            return null;
        }
        final String query = "SELECT * FROM post WHERE forum_id = ? ORDER BY date " + order + ';';
        final List<Post> posts = template.query(query, postMapper,
                forum.getId());

        return listPostToPostDetails(posts, related);
    }





    @Override
    public List<PostDetailsExtended> getPostsByThread(long threadId, LocalDateTime since, Integer limit, String order) {
        final String query = "SELECT * FROM post WHERE thread_id=? AND date >= ? ORDER BY date " + order + " LIMIT ?;";
        final List<Post> posts = template.query(query, postMapper,
                threadId, since, limit);
        return listPostToPostDetails(posts, null);
    }

    public List<PostDetailsExtended> getPostsByThread (long threadId, LocalDateTime since, String order) {
        final String query = "SELECT * FROM post WHERE thread_id=? AND date >= ? ORDER BY date " + order + ';';
        final List<Post> posts = template.query(query, postMapper,
                threadId, since);
        return listPostToPostDetails(posts, null);
    }

    public List<PostDetailsExtended> getPostsByThread (long threadId, Integer limit, String order) {
        final String query = "SELECT * FROM post WHERE thread_id=? ORDER BY date " + order + " LIMIT ?";
        final List<Post> posts = template.query(query, postMapper,
                threadId, limit);
        return listPostToPostDetails(posts, null);
    }

    public List<PostDetailsExtended> getPostsByThread (long threadId, String order) {
        final String query = "SELECT * FROM post WHERE thread_id=? ORDER BY date " + order + ';';
        final List<Post> posts = template.query(query, postMapper,
                threadId);
        return listPostToPostDetails(posts, null);
    }




    public List<PostDetailsExtended> getPostsByUser(String userEmail, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting posts by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM post WHERE user_id = ? ORDER BY date " + order + ';';
        final List<Post> posts = template.query(query, postMapper, user.getId());
        return listPostToPostDetails(posts, null);
    }

    public List<PostDetailsExtended> getPostsByUser(String userEmail, LocalDateTime since, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting posts by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM post WHERE user_id = ? AND date >= ? ORDER BY date " + order + ';';
        final List<Post> posts = template.query(query, postMapper,
                user.getId(), since);
        return listPostToPostDetails(posts, null);
    }


    public List<PostDetailsExtended> getPostsByUser(String userEmail, Integer limit, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting posts by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM post WHERE user_id = ? ORDER BY date " + order + " LIMIT ?";
        final List<Post> posts = template.query(query, postMapper,
                user.getId(), limit);
        return listPostToPostDetails(posts, null);
    }


    @Override
    public List<PostDetailsExtended> getPostsByUser(String userEmail, LocalDateTime since, Integer limit, String order) {
        final User user = userDAODataBase.getByEmail(userEmail);
        if (user == null) {
            LOGGER.info("Error getting posts by user because user {} does not exist", userEmail);
            return null;
        }
        final String query = "SELECT * FROM post WHERE user_id = ? AND date >= ? ORDER BY date " + order + " LIMIT ?;";
        final List<Post> posts = template.query(query, postMapper,
                user.getId(), since, limit);
        return listPostToPostDetails(posts, null);
    }







    @Override
    public boolean remove(long postId) {
        final String query = "UPDATE post SET deleted = ? WHERE id = ?;";
        final int affectedRows = template.update(query, true, postId);
        if (affectedRows == 0) {
            LOGGER.info("Removing post with ID={} failed", postId);
            return false;
        }
        final Post post = getById(postId);
        threadDAODataBase.removePost(post.getThreadId());
        LOGGER.info("Removed post with ID={}", postId);
        return true;
    }

    @Override
    public boolean restore(long postId) {
        final String query = "UPDATE post SET deleted = ? WHERE id = ?;";
        final int affectedRows = template.update(query, false, postId);
        if (affectedRows == 0) {
            LOGGER.info("Restoring post with ID={} failed", postId);
            return false;
        }
        final Post post = getById(postId);
        threadDAODataBase.addPost(post.getThreadId());
        LOGGER.info("Restored post with ID={}", postId);
        return true;
    }

    @Override
    public void markDeleted(long threadId) {
        final String query = "UPDATE post SET deleted = ? WHERE thread_id = ?;";
        template.update(query, true, threadId);
    }

    @Override
    public void markRestored(long threadId) {
        final String query = "UPDATE post SET deleted = ? WHERE thread_id = ?;";
        template.update(query, false, threadId);
    }


    @Override
    public PostDetailsExtended vote(long postId, int vote) {
        final String query;

        if (vote == 1) {
            query = "UPDATE post SET likes = likes + 1 WHERE id=?;";
        }
        else {
            query = "UPDATE post SET dislikes = dislikes + 1 WHERE id=?;";
        }
        final int affectedRows = template.update(query, postId);
        if (affectedRows == 0) {
            LOGGER.info("Error vote because post with ID={} does not exist!", postId);
            return null;
        }
        final Post post = getById(postId);
        return postToPostDetails(post, null);
    }

    @Override
    public PostDetailsExtended update(long postId, String message) {
        final String query = "UPDATE post SET message = ? WHERE id = ?;";
        final int affectedRows = template.update(query, message, postId);
        if (affectedRows == 0) {
            LOGGER.info("Error update post because post with ID={} does not exist!", postId);
            return null;
        }
        final Post post = getById(postId);
        return postToPostDetails(post, null);
    }



    private List<PostDetailsExtended> listPostToPostDetails (Iterable<Post> posts, Collection<String> related) {
        final List<PostDetailsExtended> result = new ArrayList<>();
        for (Post post: posts) {
            result.add(postToPostDetails(post, related));
        }
        return result;
    }


    private PostDetailsExtended postToPostDetails (Post post, Collection<String> related) {
        final PostDetailsExtended result = new PostDetailsExtended(post);

        if (related != null && related.contains("user")) {
            final UserDetailsExtended userDetails = userDAODataBase.getDetails(post.getUserId());
            result.setUser(userDetails);
        }
        else {
            final User user = userDAODataBase.getById(post.getUserId());
            result.setUser(user.getEmail());
        }

        if (related != null && related.contains("forum")) {
            final ForumDetails forumDetails = forumDAODataBase.getDetails(post.getForumId(), null);
            result.setForum(forumDetails);
        }
        else {
            final Forum forum = forumDAODataBase.getById(post.getForumId());
            result.setForum(forum.getShortName());
        }

        if (related != null && related.contains("thread")) {
            ThreadDetailsExtended threadDetails = threadDAODataBase.getDetails(post.getThreadId());
            result.setThread(threadDetails);
        }
        else {
            result.setThread(post.getThreadId());
        }
        return result;
    }

    RowMapper<Post> postMapper = (rs, i) -> {
            final String message = rs.getString("message");
            final Timestamp timestamp= rs.getTimestamp("date");
            final LocalDateTime date = timestamp.toLocalDateTime();
            final Long threadId = rs.getLong("thread_id");
            final long userId = rs.getLong("user_id");
            final long forumId = rs.getLong("forum_id");
            final Long parent =(Long) rs.getObject("parent");
            final boolean approved = rs.getBoolean("approved");
            final boolean highlighted = rs.getBoolean("highlighted");
            final boolean edited = rs.getBoolean("edited");
            final boolean spam = rs.getBoolean("spam");
            final boolean deleted = rs.getBoolean("deleted");
            final long id = rs.getLong("id");
            final String path = rs.getString("path");
            final int likes = rs.getInt("likes");
            final int dislikes = rs.getInt("dislikes");
            final Post result = new Post(message, date, threadId, userId, forumId, parent,
                    approved, highlighted, edited, spam, deleted);
            result.setPath(path);
            result.setId(id);
            result.setLikes(likes);
            result.setDislikes(dislikes);
            return result;
        };
}
