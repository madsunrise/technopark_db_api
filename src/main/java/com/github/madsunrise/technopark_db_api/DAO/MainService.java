package com.github.madsunrise.technopark_db_api.DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by ivan on 17.10.16.
 */
@Service
@Transactional
public class MainService {
    private final JdbcTemplate template;
    private final UserDAO userDAO;
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final PostDAO postDAO;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainService.class.getName());

    public MainService(JdbcTemplate template, UserDAO userDAO, ForumDAO forumDAO,
                       ThreadDAO threadDAO, PostDAO postDAO) {
        this.template = template;
        this.userDAO = userDAO;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    public void clear() {
        dropAdditional();
        postDAO.clear();
        threadDAO.clear();
        forumDAO.clear();
        userDAO.clear();
        userDAO.createTable();
        forumDAO.createTable();
        threadDAO.createTable();
        postDAO.createTable();
        createAdditional();

    }
    private void createAdditional() {
        createSubscription();
        createFollowing();
        createUserForum();
        LOGGER.info("Additional tables were created");
    }

    private void createSubscription() {
        final String createTable = "CREATE TABLE IF NOT EXISTS subscription (" +
                "user_id BIGINT NOT NULL," +
                "thread_id BIGINT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id)," +
                "FOREIGN KEY (thread_id) REFERENCES thread(id)," +
                "PRIMARY KEY (user_id, thread_id))";
        template.execute(createTable);
    }

    private void createFollowing() {
        final String createTable = "CREATE TABLE IF NOT EXISTS following (" +
                "follower_id BIGINT NOT NULL," +
                "followee_id BIGINT NOT NULL," +
                "FOREIGN KEY (follower_id) REFERENCES user(id)," +
                "FOREIGN KEY (followee_id) REFERENCES user(id)," +
                "PRIMARY KEY (follower_id, followee_id))";
        template.execute(createTable);
    }

    private void createUserForum() {
        final String createTable = "CREATE TABLE IF NOT EXISTS user_forum (" +
                "id BIGINT PRIMARY KEY auto_increment," +
                "user_id BIGINT NOT NULL," +
                "forum_id BIGINT NOT NULL," +
                "UNIQUE KEY (user_id, forum_id)," +
                "UNIQUE KEY (forum_id, user_id))";
        template.execute(createTable);

    }

    private void dropAdditional() {
        dropSubscription();
        dropFollowing();
        dropUserForum();
        LOGGER.info("Additional tables were dropped");
    }

    private void dropSubscription() {
        final String dropTable = "DROP TABLE IF EXISTS subscription";
        template.execute(dropTable);
    }

    private void dropFollowing() {
        final String dropTable = "DROP TABLE IF EXISTS following";
        template.execute(dropTable);
    }

    private void dropUserForum() {
        final String dropTable = "DROP TABLE IF EXISTS user_forum";
        template.execute(dropTable);
    }

    public long getUserAmount() {
	LOGGER.info("User count was got");
        return userDAO.getAmount();
    }

    public long getForumAmount() {
	LOGGER.info("Forum count was got");
        return forumDAO.getAmount();
    }

    public long getThreadAmount() {
	LOGGER.info("Thread count was got");
        return threadDAO.getAmount();
    }

    public long getPostAmount() {
	LOGGER.info("Post count was got");
        return postDAO.getAmount();
    }
}
