package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Post;
import com.github.madsunrise.technopark_db_api.response.PostDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
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
public class PostDAODataBaseImpl implements PostDAO {

    private final JdbcTemplate template;
    private static final Logger logger = LoggerFactory.getLogger(PostDAODataBaseImpl.class.getName());
    public PostDAODataBaseImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void clear() {
        final String dropTable = "DROP TABLE IF EXISTS post";
        template.execute(dropTable);
        logger.info("Table post was dropped");
    }

    @Override
    public void createTable() {
        final String createTable = "CREATE TABLE post (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "message TEXT NOT NULL," +
                "date DATETIME NOT NULL," +
                "user VARCHAR(30) NOT NULL," +
                "forum VARCHAR(30) NOT NULL," +
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
        logger.info("Table post was created");
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public Post getById(long id) {
        return null;
    }

    @Override
    public PostDetails create(LocalDateTime date, long threadId, String message, String userEmail, String forumShortName, Long parent, boolean approved, boolean highlighted, boolean edited, boolean spam, boolean delete) {
        return null;
    }

    @Override
    public PostDetailsExtended getDetails(long id, List<String> related) {
        return null;
    }

    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public List<PostDetailsExtended> getPostsByForum(String forumShortName, LocalDateTime since, Integer limit, String order, List<String> related) {
        return null;
    }

    @Override
    public List<PostDetailsExtended> getPostsByThread(long threadId, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public List<PostDetailsExtended> getPostsByUser(String userEmail, LocalDateTime since, Integer limit, String order) {
        return null;
    }

    @Override
    public Long remove(long postId) {
        return null;
    }

    @Override
    public Long restore(long postId) {
        return null;
    }

    @Override
    public void markDeleted(long threadId) {

    }

    @Override
    public void markRestored(long threadId) {

    }

    @Override
    public PostDetailsExtended vote(long postId, int vote) {
        return null;
    }

    @Override
    public PostDetailsExtended update(long postId, String message) {
        return null;
    }
}
