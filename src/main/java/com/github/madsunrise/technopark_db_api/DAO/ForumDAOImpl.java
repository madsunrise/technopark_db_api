package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 09.10.16.
 */
public class ForumDAOImpl implements ForumDAO {
    private static final Map<String, Forum> shortNameToForum = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ForumDAOImpl.class.getName());

    @Override
    public Forum getByShortName(String shortName) {
        return shortNameToForum.get(shortName);
    }

    @Override
    public void createTable() {

    }

    @Override
    public ForumDetails create(String name, String shortName, String userEmail) {
        Forum forum = getByShortName(shortName);
        if (forum != null) {
            logger.info("Error creating forum \"{}\": it's already exists!", name);
        }
        else {
            User user = new UserDAOImpl().getByEmail(userEmail);
            if (user == null) {
                logger.info("Error creating forum \"{}\": user \"{}\" does not exist!", name, userEmail);
                return null;
            }
            forum = new Forum(name, shortName, userEmail, user.getId());
            shortNameToForum.put(shortName, forum);
            logger.info("Forum \"{}\" was successful created", name);
        }
        return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), forum.getUser());
    }

    @Override
    public ForumDetails getDetails(String shortName) {
        return  this.getDetails(shortName, null);
    }

    @Override
    public ForumDetails getDetails(String shortName, List<String> related) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            logger.info("Error getting forum details because forum \"{}\": does not exist!", shortName);
            return null;
        }
        logger.info("Getting forum details \"{}\" is success", shortName);
        if (related != null && related.contains("user")) {
            User user = new UserDAOImpl().getByEmail(forum.getUser());
            UserDetailsExtended userDetails = new UserDetailsExtended(user);
            return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), userDetails);
        }

        return new ForumDetails<>(forum.getId(), forum.getName(), forum.getShortName(), forum.getUser());
    }

    @Override
    public void clear() {
        shortNameToForum.clear();
    }

    @Override
    public long getAmount() {
        return shortNameToForum.size();
    }


    @Override
    public List<PostDetailsExtended> getPosts(String shortName, LocalDateTime since,
                                              Integer limit, String order, List<String> related) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            logger.info("Error getting forum's posts because forum \"{}\": does not exist!", shortName);
            return null;
        }
        return new PostDAOImpl().getPostsByForum(shortName, since, limit, order, related);
    }

    @Override
    public List<ThreadDetailsExtended> getThreads(String shortName, LocalDateTime since,
                                                  Integer limit, String order, List<String> related) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            logger.info("Error getting forum's threads because forum \"{}\": does not exist!", shortName);
            return null;
        }
        return new ThreadDAOImpl().getThreadsByForum(shortName, since, limit, order, related);
    }


    public List<UserDetailsExtended> getUsers(String shortName, Long sinceId, Integer limit, String order) {
        final Forum forum = getByShortName(shortName);
        if (forum == null) {
            logger.info("Error getting forum's users because forum \"{}\": does not exist!", shortName);
            return null;
        }
        return new UserDAOImpl().getUsersByForum(shortName, sinceId, limit, order);
    }
}
