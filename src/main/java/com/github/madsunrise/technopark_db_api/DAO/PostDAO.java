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
public class PostDAO  {

    private final JdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostDAO.class.getName());
    public PostDAO(JdbcTemplate template) {
        this.template = template;
    }

    @Autowired
    private UserDAO userDAODataBase;
    @Autowired
    private ForumDAO forumDAODataBase;
    @Autowired
    private ThreadDAO threadDAODataBase;


    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS post";
        template.execute(dropTable);
        LOGGER.info("Table post was dropped");
    }


    public void createTable() {
        final String createTable = "CREATE TABLE post (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "message TEXT NOT NULL," +
                "date DATETIME NOT NULL," +
                "parent BIGINT," +
                "root BIGINT," +    // ID самого первого поста в иерархии (для корневых постов это свой ID)
                "path VARCHAR(100)," + // уже не включает самый верхний уровень
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


    public long getAmount() {
        final String query = "SELECT COUNT(*) FROM post;";
        return template.queryForObject(query, Long.class);
    }


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


    public PostDetails create(LocalDateTime date, long threadId, String message, String userEmail,
                              String forumShortName, Long parent, boolean approved,
                              boolean highlighted, boolean edited, boolean spam, boolean deleted) {
        final long start = System.currentTimeMillis();
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

        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(new PostPstCreator(post), keyHolder);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating post because it already exists!");
            return null;
        }

        final Map<String, Object> keys = keyHolder.getKeys();
        post.setId((long)keys.get("GENERATED_KEY"));

        setPathAndRoot(post);
        final String updatePost = "UPDATE post SET path = ?, root = ? WHERE id = ?;";
        template.update(updatePost, post.getPath(), post.getRoot(), post.getId());


        // Updating thread
        final String threadQuery = "UPDATE thread SET posts = posts + 1 WHERE id=?;";
        template.update(threadQuery, threadId);

        // Updating user_forum
        try {
            final String userForumQuery = "INSERT INTO user_forum (user_id, forum_id) VALUES (?, ?)";
            template.update(userForumQuery, user.getId(), forum.getId());
        }
        catch (DuplicateKeyException e) {
            
        }


        final PostDetails<String, String, Long> postDetails = new PostDetails<>(post);
        postDetails.setForum(forumShortName);
        postDetails.setUser(userEmail);
        postDetails.setThread(threadId);

        final long end = System.currentTimeMillis();
//       LOGGER.info("Post with id={} successful created, time: {}", post.getId(), end-start);
        return postDetails;
    }

    private static class PostPstCreator implements PreparedStatementCreator {
        private final Post post;
        PostPstCreator(Post post) {
            this.post = post;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO post (message, date, thread_id, user_id, forum_id," +
                    "path, parent, root, approved, highlighted, edited, spam, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, post.getMessage());
            final Timestamp date = Timestamp.valueOf(post.getDate());
            pst.setTimestamp(2, date);
            pst.setLong(3, post.getThreadId());
            pst.setLong(4, post.getUserId());
            pst.setLong(5, post.getForumId());

            String path = post.getPath();
            if (path != null) {
                pst.setString(6, path);
            }
            else {
                pst.setNull(6, Types.VARCHAR);
            }

            final Long parent = post.getParent();
            if (parent != null) {
                pst.setLong(7, post.getParent());
            }
            else {
                pst.setNull(7, Types.BIGINT);
            }

            pst.setLong(8, Types.BIGINT);
            pst.setBoolean(9, post.isApproved());
            pst.setBoolean(10, post.isHighlighted());
            pst.setBoolean(11, post.isEdited());
            pst.setBoolean(12, post.isSpam());
            pst.setBoolean(13, post.isDeleted());
            return pst;
        }
    }


    private void setPathAndRoot (Post post) {
        if (post.getParent() == null) {
            post.setRoot(post.getId());
            post.setPath(null);
            return;
        }

        final StringBuilder result = new StringBuilder();
        final Post parent = getById(post.getParent());
        post.setRoot(parent.getRoot());

        final String parentPath = parent.getPath();
        if (parentPath != null) {
            result.append(parentPath);
            result.append('.');
        }


        final int index = getMaxChildIndex(post.getParent()) + 1;
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
        post.setPath(result.toString());
    }


    private int getMaxChildIndex (long parentId) {
        try {
            final String query = "SELECT path FROM post WHERE parent = ? ORDER BY path DESC LIMIT 1";
            final String path = template.queryForObject(query, String.class, parentId);
            final String[] parts = path.split("\\.");
            return Integer.parseInt(parts[parts.length - 1]);
        }
        catch (EmptyResultDataAccessException | NullPointerException e) {
 //           LOGGER.info("No paths found");
            return -1;
        }
    }



    public PostDetailsExtended getDetails(long id) {
        return this.getDetails(id, null);
    }


    public PostDetailsExtended getDetails(long id, List<String> related) {
        final Post post = getById(id);
        if (post == null) {
            LOGGER.info("Error getting post details - post with ID={}: does not exist!", id);
            return null;
        }
   //     LOGGER.info("Getting post (ID={}) details is success", id);
        return postToPostDetails (post, related);
    }






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






    public List<PostDetailsExtended> getPostsByThread(long threadId, LocalDateTime since,
                                                      String sortType, String order, Integer limit) {
        final StringBuilder query = new StringBuilder("SELECT * FROM post WHERE thread_id = ? ");

        if (since != null) {
            query.append("AND date >= ? ");
        }

        String sortPart;
        switch (sortType) {
            case "flat":
                sortPart = "date " + order;
                break;
            case "tree":
            case "parent_tree":
                sortPart = "root " + order + ", path ASC";
                break;
            default:
                sortPart = "";
        }

        query.append("ORDER BY ");
        query.append(sortPart);

        if (limit != null && !sortType.equals("parent_tree")) {
            query.append(" LIMIT ?");
        }

        List<Post> posts;
        if (since != null) {
            if (limit != null && !sortType.equals("parent_tree")) {
                posts = template.query(query.toString(), postMapper,
                        threadId, since, limit);
            }
            else {
                posts = template.query(query.toString(), postMapper,
                        threadId, since);
            }
        }
        else {
            if (limit != null && !sortType.equals("parent_tree")) {
                posts = template.query(query.toString(), postMapper,
                        threadId, limit);
            }
            else {
                posts = template.query(query.toString(), postMapper,
                        threadId);
            }
        }

        // Slice in case parent tree
        if (limit != null && sortType.equals("parent_tree")) {
            int rootCount = 0;
            int postsCount = 0;
            for (Post post: posts) {
                if (post.getParent() == null) {  // Перед нами корневой пост
                    rootCount++;
                    if (rootCount > limit) {
                        break;
                    }
                }
                postsCount++;
            }
            posts = posts.subList(0, postsCount);
        }

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








    public boolean remove(long postId) {
        final String query = "UPDATE post SET deleted = ? WHERE id = ?;";
        final int affectedRows = template.update(query, true, postId);
        if (affectedRows == 0) {
            LOGGER.info("Removing post with ID={} failed", postId);
            return false;
        }
        final Post post = getById(postId);
        threadDAODataBase.removePost(post.getThreadId());
     //   LOGGER.info("Removed post with ID={}", postId);
        return true;
    }


    public boolean restore(long postId) {
        final String query = "UPDATE post SET deleted = ? WHERE id = ?;";
        final int affectedRows = template.update(query, false, postId);
        if (affectedRows == 0) {
            LOGGER.info("Restoring post with ID={} failed", postId);
            return false;
        }
        final Post post = getById(postId);
        threadDAODataBase.addPost(post.getThreadId());
       // LOGGER.info("Restored post with ID={}", postId);
        return true;
    }


    public void markDeleted(long threadId) {
        final String query = "UPDATE post SET deleted = ? WHERE thread_id = ?;";
        template.update(query, true, threadId);
    }


    public void markRestored(long threadId) {
        final String query = "UPDATE post SET deleted = ? WHERE thread_id = ?;";
        template.update(query, false, threadId);
    }



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

    private RowMapper<Post> postMapper = (rs, i) -> {
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
            final String path = (String) rs.getObject("path");
            final long root = rs.getLong("root");
            final int likes = rs.getInt("likes");
            final int dislikes = rs.getInt("dislikes");
            final Post result = new Post(message, date, threadId, userId, forumId, parent,
                    approved, highlighted, edited, spam, deleted);
            result.setPath(path);
            result.setRoot(root);
            result.setId(id);
            result.setLikes(likes);
            result.setDislikes(dislikes);
            return result;
        };
}
