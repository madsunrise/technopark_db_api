package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.Post;
import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 11.10.16.
 */
public class ThreadDAOImpl implements ThreadDAO {
    private static final Map<Long, Thread> idToThread = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ThreadDAOImpl.class.getName());

    @Override
    public void addPost(long threadId) {

    }

    @Override
    public Thread getById(long id) {
        return idToThread.get(id);
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
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error closing thread with ID={} because it does not exist", threadId);
            return null;
        }
        thread.setClosed(true);
        logger.info("Closed thread with ID={}", threadId);
        return thread.getId();
    }

    @Override
    public Long open(long threadId) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error opening thread with ID={} because it does not exist!", threadId);
            return null;
        }
        thread.setClosed(false);
        logger.info("Opened thread with ID={}", threadId);
        return thread.getId();
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId) {
       return this.getDetails(threadId, null);
    }

    @Override
    public ThreadDetailsExtended getDetails(long threadId, List<String> related)
    {
        final Thread thread = getById(threadId);
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

    @Override
    public Long remove(long threadId) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error removing thread with ID={} because it does not exist!", threadId);
            return null;
        }
        thread.setDeleted(true);

        new PostDAOImpl().markDeleted(threadId);

        logger.info("Removing thread with ID={}", threadId);
        return thread.getId();
    }

    @Override
    public Long restore(long threadId) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error restoring thread with ID={} because it does not exist!", threadId);
            return null;
        }
        thread.setDeleted(false);

        new PostDAOImpl().markRestored(threadId);

        logger.info("Restoring thread with ID={}", threadId);
        return thread.getId();
    }

    @Override
    public Long subscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error subscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = new UserDAOImpl().subscribe(threadId, userEmail);
        if (userId == null) {
            logger.info("Error subscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        return thread.getId();
    }

    @Override
    public Long unsubscribe(long threadId, String userEmail) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error unsubscribing user because thread with ID={} does not exist!", threadId);
            return null;
        }
        final Long userId = new UserDAOImpl().unsubscribe(threadId, userEmail);
        if (userId == null) {
            logger.info("Error unsubscribing user because user with email={} does not exist!", userEmail);
            return null;
        }
        return thread.getId();
    }



    @Override
    public List<PostDetailsExtended> getPosts(long threadId, LocalDateTime since, Integer limit, String order, String sort) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error getting post list because thread with ID={} does not exist!", threadId);
            return null;
        }

        final List<PostDetailsExtended> posts = new PostDAOImpl().getPostsByThread(threadId, since, null, order);

        // Add sort here
        if (sort.equals("flat")) {
            if (order.equals("asc")) {
                Collections.sort(posts, new DatePostsComparator());
            }
            else {
                Collections.sort(posts, Collections.reverseOrder(new DatePostsComparator()));
            }
        }

        if (sort.equals("tree") || sort.equals("parent_tree")) {
            if (order.equals("asc")) {
                Collections.sort(posts, new PathComparatorAsc());
            }
            else {
                Collections.sort(posts, new PathComparatorDesc());
            }
        }

        if (limit != null && limit < posts.size()) {
            if (sort.equals("parent_tree")) {
                int rootCount = 0;
                int postsCount = 0;
                for (PostDetailsExtended postDetails: posts) {
                    String path = postDetails.getPath();
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

            return posts.subList(0, limit); // для flat и tree
        }

        return posts;
    }



    @Override
    public ThreadDetailsExtended vote(long threadId, int vote) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error vote because thread with ID={} does not exist!", threadId);
            return null;
        }
        if (vote == 1) {
            thread.like();
        }
        if (vote == -1) {
            thread.dislike();
        }
        // save
        return new ThreadDetailsExtended(thread);
    }

    @Override
    public void createTable() {

    }

    @Override
    public ThreadDetailsExtended update(long threadId, String message, String slug) {
        final Thread thread = getById(threadId);
        if (thread == null) {
            logger.info("Error updating because thread with ID={} does not exist!", threadId);
            return null;
        }
        thread.setMessage(message);
        thread.setSlug(slug);
        return new ThreadDetailsExtended(thread);
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since, Integer limit, String order) {
        return this.getThreadsByForum(forumShortName, since, limit, order, null);
    }

    @Override
    public List<ThreadDetailsExtended> getThreadsByForum(String forumShortName, LocalDateTime since, Integer limit, String order, List<String> related) {
        List<ThreadDetailsExtended> threads = new ArrayList<>();
        for (Map.Entry<Long, Thread> entry: idToThread.entrySet()) {
            final Thread thread = entry.getValue();

            if (thread.getForum().equals(forumShortName)) {
                final ThreadDetailsExtended threadDetails = new ThreadDetailsExtended(thread);

                if (related != null && related.contains("forum")) {
                    final ForumDetails forumDetails = new ForumDAOImpl().getDetails(forumShortName);
                    threadDetails.setForum(forumDetails);
                }
                else {
                    threadDetails.setForum(forumShortName);
                }

                if (related != null && related.contains("user")) {
                    final UserDetailsExtended userDetails = new UserDAOImpl().getDetails(thread.getUser());
                    threadDetails.setUser(userDetails);
                }
                else {
                    threadDetails.setUser(thread.getUser());
                }

                threads.add(threadDetails);
            }
        }

        // отсекаем старые треды
        if (since != null) {
            final List<ThreadDetailsExtended> temp = new ArrayList<>();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (ThreadDetailsExtended threadDetails: threads) {
                final String dateStr = threadDetails.getDate();
                final LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
                if (date.compareTo(since) >= 0) {
                    temp.add(threadDetails);
                }
            }
            threads = temp;
        }

        // Sort по дате
        if (order.equals("asc")) {
            Collections.sort(threads, new DateComparator());
        }
        else {
            Collections.sort(threads, Collections.reverseOrder(new DateComparator()));
        }


        if (limit == null || limit > threads.size()) {
            limit = threads.size();
        }

        logger.info("Getting list threads success");
        return threads.subList(0, limit);
    }


    @Override
    public List<ThreadDetailsExtended> getThreadsByUser(String userEmail, LocalDateTime since, Integer limit, String order) {
        List<ThreadDetailsExtended> threads = new ArrayList<>();
        for (Map.Entry<Long, Thread> entry: idToThread.entrySet()) {
            final Thread thread = entry.getValue();

            if (thread.getUser().equals(userEmail)) {
                final ThreadDetailsExtended threadDetails = new ThreadDetailsExtended(thread);
                threadDetails.setForum(thread.getForum());
                threadDetails.setUser(userEmail);
                threads.add(threadDetails);
            }
        }

        // отсекаем старые треды
        if (since != null) {
            final List<ThreadDetailsExtended> temp = new ArrayList<>();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (ThreadDetailsExtended threadDetails: threads) {
                final String dateStr = threadDetails.getDate();
                final LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
                if (date.compareTo(since) >= 0) {
                    temp.add(threadDetails);
                }
            }
            threads = temp;
        }

        // Sort по дате
        if (order.equals("asc")) {
            Collections.sort(threads, new DateComparator());
        }
        else {
            Collections.sort(threads, Collections.reverseOrder(new DateComparator()));
        }


        if (limit == null || limit > threads.size()) {
            limit = threads.size();
        }

        logger.info("Getting list threads success");
        return threads.subList(0, limit);
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
}
