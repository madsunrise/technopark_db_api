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
 * Created by ivan on 12.10.16.
 */


// При добавлении поста инкрементим счетчик в Thread!
public class PostDAOImpl implements PostDAO {
    private static final Map<Long, Post> idToPost = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ThreadDAOImpl.class.getName());


    @Override
    public Post getById(long id) {
        final Post post = idToPost.get(id);
        if (post == null) {
            logger.info("Post with ID={} not found!", id);
            return null;
        }
        return post;
    }

    @Override
    public PostDetails create(LocalDateTime date, long threadId, String message, String userEmail, String forumShortName,
                              Long parent, boolean approved, boolean highlighted, boolean edited, boolean spam,
                              boolean deleted) {
        final Forum forum = new ForumDAOImpl().getByShortName(forumShortName);
        if (forum == null) {
            logger.info("Error creating post because forum \"{}\" does not exist!", forumShortName);
            return null;
        }
        final User user = new UserDAOImpl().getByEmail(userEmail);
        if (user == null) {
            logger.info("Error creating post because user \"{}\" does not exist!", userEmail);
            return null;
        }
        final Thread thread = new ThreadDAOImpl().getById(threadId);
        if (thread == null) {
            logger.info("Error creating post because thread with ID=\"{}\" does not exist!", threadId);
            return null;
        }

        final Post post = new Post(message, date, threadId, userEmail, user.getId(), forumShortName, forum.getId(),
                parent, approved, highlighted, edited, spam, deleted);
        idToPost.put(post.getId(), post);

        logger.info("Post with ID={} and forum={} successful created", post.getId(), post.getForum());

        // меняем также thread
        thread.addPost();
        new ThreadDAOImpl().save(thread);

        final PostDetails<String, String, Long> postDetails = new PostDetails<>(post);
        postDetails.setForum(post.getForum());
        postDetails.setUser(post.getUser());
        postDetails.setThread(post.getThreadId());
        return postDetails;
    }

    @Override
    public PostDetailsExtended getDetails(long id, List<String> related) {
        final Post post = getById(id);
        if (post == null) {
            logger.info("Error getting post details - post with ID={}: does not exist!", id);
            return null;
        }
        final PostDetailsExtended result = new PostDetailsExtended(post);

        if (related != null && related.contains("user")) {
            final UserDetailsExtended userDetails = new UserDAOImpl().getDetails(post.getUser());
            result.setUser(userDetails);
        }
        else {
            result.setUser(post.getUser());
        }

        if (related != null && related.contains("forum")) {
            final ForumDetails forumDetails = new ForumDAOImpl().getDetails(post.getForum(), null);
            result.setForum(forumDetails);
        }
        else {
            result.setForum(post.getForum());
        }

        if (related != null && related.contains("thread")) {
            ThreadDetailsExtended threadDetails = new ThreadDAOImpl().getDetails(post.getThreadId(), null);
            result.setThread(threadDetails);
        }
        else {
            result.setThread(post.getThreadId());
        }

        logger.info("Getting post (ID={}) details is success", id);
        return result;
    }

    @Override
    public void clear() {
        logger.info("Truncate all posts success");
        idToPost.clear();
    }

    @Override
    public long getAmount() {
        return idToPost.size();
    }


    @Override
    public List<PostDetailsExtended> getPostsDetails(String forumShortName,
                                                     LocalDateTime since, Integer limit, String order) {
        List<PostDetailsExtended> allPosts = new ArrayList<>();
        for (Map.Entry<Long, Post> entry: idToPost.entrySet()) {
            final Post post = entry.getValue();
            if (post.getForum().equals(forumShortName)) {
                final PostDetailsExtended postDetails = new PostDetailsExtended(post);
                postDetails.setUser(post.getUser());
                postDetails.setForum(forumShortName);
                postDetails.setThread(post.getThreadId());
                allPosts.add(postDetails);
            }
        }

        // отсекаем старые посты
        if (since != null) {
            final List<PostDetailsExtended> temp = new ArrayList<>();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (PostDetailsExtended postDetails: allPosts) {
                final String dateStr = postDetails.getDate();
                final LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
                if (date.compareTo(since) >= 0) {
                    temp.add(postDetails);
                }
            }
            allPosts = temp;
        }

        // Sort по дате
        if (order.equals("desc")) {
            Collections.sort(allPosts, Collections.reverseOrder(new DateComparator()));
        }


        final List<PostDetailsExtended> result = new ArrayList<>();
        if (limit == null) {
            limit = allPosts.size();
        }
        for (int i = 0; i < allPosts.size() && i < limit; ++i) {
            result.add(allPosts.get(i));
        }

        logger.info("Getting list posts success");
        return result;
    }

    @Override
    public List<PostDetailsExtended> getPostsDetails(long threadId,
                                                     LocalDateTime since, Integer limit, String order) {
        final Thread thread = new ThreadDAOImpl().getById(threadId);
        if (thread == null) {
            logger.info("Error getting list posts because thread with ID={} does not exists", threadId);
            return null;
        }
        return this.getPostsDetails(thread.getForum(), since, limit, order);
    }


    static class IdComparator implements Comparator<PostDetailsExtended> {
        @Override
        public int compare(PostDetailsExtended p1, PostDetailsExtended p2) {
            return (int) (p1.getId() - p2.getId());
        }
    }

    static class DateComparator implements Comparator<PostDetailsExtended> {
        @Override
        public int compare(PostDetailsExtended p1, PostDetailsExtended p2) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime d1 = LocalDateTime.parse(p1.getDate(), formatter);
            final LocalDateTime d2 = LocalDateTime.parse(p2.getDate(), formatter);
            return d1.compareTo(d2);
        }
    }


    @Override
    public void markDeleted(long threadId) {
        for (Map.Entry<Long, Post> entry: idToPost.entrySet()) {
            final Post post = entry.getValue();
            if (post.getThreadId() == threadId) {
                post.setDeleted(true);
            }
        }
    }


    @Override
    public Long remove(long postId) {
        final Post post = idToPost.get(postId);
        if (post == null) {
            logger.info("Error removing post with ID={} because it does not exist!", postId);
            return null;
        }
        post.setDeleted(true);
        final Thread thread = new ThreadDAOImpl().getById(post.getThreadId());
        thread.removePost();
        logger.info("Removing post with ID={}", postId);
        return post.getId();
    }

    @Override
    public Long restore(long postId) {
        final Post post = idToPost.get(postId);
        if (post == null) {
            logger.info("Error restoring post with ID={} because it does not exist!", postId);
            return null;
        }
        post.setDeleted(false);
        final Thread thread = new ThreadDAOImpl().getById(post.getThreadId());
        thread.addPost();
        logger.info("Restoring post with ID={} is success", postId);
        return post.getId();
    }
}
