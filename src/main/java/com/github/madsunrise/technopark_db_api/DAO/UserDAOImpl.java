package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Post;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.PostDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 08.10.16.
 */
public class UserDAOImpl implements UserDAO {
    private static final Map<Long, User> idToUser = new ConcurrentHashMap<>();
    private static final Map<String, User> emailToUser = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ForumDAOImpl.class.getName());


    @Override
    public User getById(Long id) {
        final User user = idToUser.get(id);
        if (user == null) {
            logger.info("User with id = \"{}\" not found!", id);
            return null;
        }
        return user;
    }

    @Override
    public User getByEmail(String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("User with email = \"{}\" not found!", email);
            return null;
        }
        return user;
    }

    @Override
    public UserDetails create(String username, String name, String email, String about, boolean anonymous) {
        User user = emailToUser.get(email);
        if (user != null) {
            logger.info("Error creating user - user with email \"{}\" already exists!", email);
            return null;
        }

        user = new User(username, name, email, about, anonymous);
        idToUser.put(user.getId(), user);
        emailToUser.put(user.getEmail(), user);
        logger.info("User with email \"{}\" successful created", email);
        return new UserDetails(user);
    }


    @Override
    public UserDetailsExtended getDetails(String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error getting user details - user with email \"{}\" does not exist!", email);
            return null;
        }
        logger.info("Getting user \"{}\" details is success", email);
        return new UserDetailsExtended(user);
    }


    @Override
    public UserDetailsExtended follow(String followerEmail, String followeeEmail) {
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null || follower.equals(followee)) {
            logger.info("Error following!");
            return null;
        }
        follower.addFollowee(followeeEmail);
        followee.addFollower(followerEmail);
        logger.info("{} now follow {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }


    @Override
    public UserDetailsExtended unfollow(String followerEmail, String followeeEmail) {
        final User follower = getByEmail(followerEmail);
        final User followee = getByEmail(followeeEmail);
        if (follower == null || followee == null) {
            logger.info("Error unfollowing - user does not exist!");
            return null;
        }
        follower.removeFollowee(followeeEmail);
        followee.removeFollower(followerEmail);
        logger.info("{} now unfollowed {}", followerEmail, followeeEmail);
        return new UserDetailsExtended(follower);
    }

    @Override
    public UserDetailsExtended getFollowers(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error getting followers - user does not exist!");
            return null;
        }

        List<String> followers = new ArrayList<>();
        followers.addAll(user.getFollowers());
        if (followers.isEmpty()) {
            new UserDetailsExtended(user);
        }

        // разрулим неопределенности
        if (order == null) {
            return null;
        }

        if (sinceId != null) {
            final List<String> cutted = new ArrayList<>();
            for (String followerEmail : followers) {
                final User follower = emailToUser.get(followerEmail);
                if (follower.getId() >= sinceId) {
                    cutted.add(followerEmail);
                }
            }
            if (cutted.isEmpty()) {
                new UserDetailsExtended(user);
            }
            followers = cutted;
        }

        if (limit == null || limit > followers.size()) {
            limit = followers.size();
        }

        // c учетом лимита
        followers = followers.subList(0, limit);

        if (order.equals("asc")) {
            Collections.sort(followers);
        } else {
            Collections.sort(followers, Collections.reverseOrder());
        }

        final UserDetailsExtended result = new UserDetailsExtended(user);
        result.setFollowers(followers);
        logger.info("Successful getting followers for \"{}\"", email);
        return result;
    }

    @Override
    public UserDetailsExtended getFollowing(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error getting followers - user does not exist!");
            return null;
        }

        List<String> following = new ArrayList<>();
        following.addAll(user.getFollowing());
        if (following.isEmpty()) {
            new UserDetailsExtended(user);
        }

        // разрулим неопределенности
        if (order == null) {
            return null;
        }

        if (sinceId != null) {
            final List<String> cutted = new ArrayList<>();
            for (String followerEmail : following) {
                final User follower = emailToUser.get(followerEmail);
                if (follower.getId() >= sinceId) {
                    cutted.add(followerEmail);
                }
            }
            if (cutted.isEmpty()) {
                new UserDetailsExtended(user);
            }
            following = cutted;
        }

        if (limit == null || limit > following.size()) {
            limit = following.size();
        }

        // c учетом лимита
        following = following.subList(0, limit);

        if (order.equals("asc")) {
            Collections.sort(following);
        } else {
            Collections.sort(following, Collections.reverseOrder());
        }

        final UserDetailsExtended result = new UserDetailsExtended(user);
        result.setFollowing(following);
        logger.info("Successful getting following for \"{}\"", email);
        return result;
    }






    @Override
    public UserDetailsExtended updateProfile(String email, String name, String about) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error update profile - user with email \"{}\" does not exist!", email);
            return null;
        }

        user.setName(name);
        user.setAbout(about);
        // UPDATE 2 COLUMNS..

        emailToUser.put(email, user);
        idToUser.put(user.getId(), user);

        logger.info("Successful updated profile user with email \"{}\"", email);
        return new UserDetailsExtended(user);
    }


    @Override
    public void clear() {
        logger.info("Truncate all users success");
        idToUser.clear();
        emailToUser.clear();
    }

    @Override
    public long getAmount() {
        return idToUser.size();
    }

    @Override
    public Long subscribe(long threadId, String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error subscribing - user with email \"{}\" does not exist!", email);
            return null;
        }
        user.subscribe(threadId);
        return user.getId();
    }

    @Override
    public Long unsubscribe(long threadId, String email) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error unsubscribing - user with email \"{}\" does not exist!", email);
            return null;
        }
        user.unsubscribe(threadId);
        return user.getId();
    }


    @Override
    public List<PostDetailsExtended> getPosts(String email, LocalDateTime since, Integer limit, String order) {
        final User user = emailToUser.get(email);
        if (user == null) {
            logger.info("Error getting posts - user with email \"{}\" does not exist!", email);
            return null;
        }
        return new PostDAOImpl().getPostsByUser(email, since, limit, order);
    }

    @Override
    public List<UserDetailsExtended> getUsersByForum(String forumShortName, Long sinceId, Integer limit, String order) {
        List<PostDetailsExtended> posts = new PostDAOImpl().getPostsByForum(forumShortName, null, null, null);
        if (posts.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserDetailsExtended> result = new ArrayList<>();

        for (PostDetailsExtended post: posts) {
            final String email = (String) post.getUser();
            final User user = getByEmail(email);
            final UserDetailsExtended userDetails = new UserDetailsExtended(user);
            result.add(userDetails);
        }

        // отсекаем ненужных
        if (sinceId != null) {
            final List<UserDetailsExtended> temp = new ArrayList<>();

            for (UserDetailsExtended userDetails: result) {
                if (userDetails.getId() >= sinceId) {
                    temp.add(userDetails);
                }
            }
            result = temp;
        }

        // Sort по имени
        if (order != null && order.equals("asc")) {
            Collections.sort(result, new NameComparator());
        }
        else {
            Collections.sort(result, Collections.reverseOrder(new NameComparator()));
        }


        if (limit == null || limit > result.size()) {
            limit = result.size();
        }

        logger.info("Getting forum's users success");
        return result.subList(0, limit);
    }

    static class NameComparator implements Comparator<UserDetailsExtended> {
        @Override
        public int compare(UserDetailsExtended u1, UserDetailsExtended u2) {
            return u1.getName().compareTo(u2.getName());
        }
    }
}
