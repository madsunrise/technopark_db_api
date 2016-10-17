package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

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
        return 0;
    }


    @Override
    public Forum getByShortName(String shortName) {
        return null;
    }

    @Override
    public ForumDetails create(String name, String shortName, String user) {
        return null;
    }

    @Override
    public ForumDetails getDetails(String shortName) {
        return null;
    }

    @Override
    public ForumDetails getDetails(String shortName, String related) {
        return null;
    }



    @Override
    public List<PostDetailsExtended> getPosts(String shortName, LocalDateTime since, Integer limit, String order, List<String> related) {
        return null;
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
