package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 11.10.16.
 */
public class ThreadDAOImpl implements ThreadDAO {
    private static final Map<Long, Thread> idToThread = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ThreadDAOImpl.class.getName());

    @Override
    public Thread getById(long id) {
        final Thread thread = idToThread.get(id);
        if (thread == null) {
            logger.info("Thread with ID={} not found!", id);
            return null;
        }
        return thread;
    }

    @Override
    public ThreadDetails create(String forumName, String title, boolean closed, String userEmail,
                                LocalDateTime date, String message, String slug, /*optional*/ boolean deleted) {
        // чек на существование форума
        final Forum forum = new ForumDAOImpl().getByShortName(forumName);
        if (forum == null) {
            logger.info("Error creating thread because forum \"{}\" does not exist!", forumName);
            return null;
        }
        final User user = new UserDAOImpl().getByEmail(userEmail);
        if (user == null) {
            logger.info("Error creating thread because user \"{}\" does not exist!", userEmail);
            return null;
        }

        final Thread thread = new Thread(title, message, date, slug,
                userEmail, user.getId(), forumName, forum.getId(), closed, deleted);
        idToThread.put(thread.getId(), thread);
        logger.info("Thread \"{}\" successful created, ID = {}", title, thread.getId());

        final ThreadDetails<String, String> threadDetails = new ThreadDetails<>(thread);
        threadDetails.setForum(thread.getForum());
        threadDetails.setUser(thread.getUser());
        return  threadDetails;
    }

    @Override
    public Long close(long threadId) {
        final Thread thread = idToThread.get(threadId);
        if (thread == null) {
            logger.info("Error closing thread with ID={}", threadId);
            return null;
        }
        thread.setClosed(true);
        logger.info("Closed thread with ID={}", threadId);
        return thread.getId();
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId, List<String> related)
    {
        final Thread thread = idToThread.get(threadId);
        if (thread == null) {
            logger.info("Error getting thread details - thread with ID={}: does not exist!", threadId);
            return null;
        }
        final ThreadDetailsExtended result = new ThreadDetailsExtended(thread);

        if (related != null && related.contains("user")) {
            User user = new UserDAOImpl().getByEmail(thread.getUser());
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            result.setUser(userDetails);
        }
        else {
            result.setUser(thread.getUser());
        }

        if (related != null && related.contains("forum")) {
            Forum forum = new ForumDAOImpl().getByShortName(thread.getForum());
            ForumDetails<String> forumDetails = new ForumDetails<>(forum);
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
    public void clear() {
        logger.info("Truncate all threads success");
        idToThread.clear();
    }

    @Override
    public long getAmount() {
        return idToThread.size();
    }

    @Override
    public long save(Thread thread) {
        final long id = thread.getId();
        idToThread.put(id, thread);
        return id;
    }
}
