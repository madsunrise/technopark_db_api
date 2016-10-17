package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetails;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
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
public class ThreadDAODataBaseImpl implements ThreadDAO{
    private final JdbcTemplate template;
    private static final Logger logger = LoggerFactory.getLogger(ThreadDAODataBaseImpl.class.getName());
    public ThreadDAODataBaseImpl(JdbcTemplate template) {
        this.template = template;
    }


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
        return 0;
    }

    @Override
    public Thread getById(long id) {
        return null;
    }

    @Override
    public ThreadDetails create(String forum, String title, boolean closed, String userEmail, LocalDateTime date, String message, String slug, boolean deleted) {
        return null;
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
        return null;
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId, List<String> related) {
        return null;
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
        return null;
    }

    @Override
    public Long unsubscribe(long threadId, String userEmail) {
        return null;
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
