package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public List<UserDetailsExtended> getFollowers(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error getting followers - user does not exist!");
            return null;
        }

        final Set<String> followersEmails = new HashSet<>();
        followersEmails.addAll(user.getFollowers());
        if (followersEmails.isEmpty()) {
            return null;
        }

        
        if (order == null) {
            order = "desc";
        }
        
        List<UserDetailsExtended> followers = new ArrayList<>();
        for (String followerEmail: followersEmails) {
            final User follower = getByEmail(followerEmail);
            followers.add(new UserDetailsExtended(follower));
        }
        

        if (sinceId != null) {
            final List<UserDetailsExtended> cutted = new ArrayList<>();
            for (UserDetailsExtended follower: followers) {
                if (follower.getId() >= sinceId) {
                    cutted.add(follower);
                }
            }
            if (cutted.isEmpty()) {
                return null;
            }
            followers = cutted;
        }
        

        if (order.equals("asc")) {
            Collections.sort(followers, new NameComparator());
        } else {
            Collections.sort(followers, Collections.reverseOrder(new NameComparator()));
        }

        if (limit == null || limit > followers.size()) {
            limit = followersEmails.size();
        }

        logger.info("Successful getting followers for \"{}\"", email);
        // c учетом лимита
        return followers.subList(0, limit);
    }

    @Override
    public List<UserDetailsExtended> getFollowees(String email, Integer limit, String order, Integer sinceId) {
        final User user = getByEmail(email);
        if (user == null) {
            logger.info("Error getting followees - user does not exist!");
            return null;
        }

        final Set<String> followeesEmail = new HashSet<>();
        followeesEmail.addAll(user.getFollowees());
        


        if (order == null) {
            order = "desc";
        }

        List<UserDetailsExtended> followees = new ArrayList<>();
        for (String followeeEmail: followeesEmail) {
            final User follower = getByEmail(followeeEmail);
            followees.add(new UserDetailsExtended(follower));
        }


        if (sinceId != null) {
            final List<UserDetailsExtended> cutted = new ArrayList<>();
            for (UserDetailsExtended followee: followees) {
                if (followee.getId() >= sinceId) {
                    cutted.add(followee);
                }
            }
            if (cutted.isEmpty()) {
                return cutted;
            }
            followees = cutted;
        }


        if (order.equals("asc")) {
            Collections.sort(followees, new NameComparator());
        } else {
            Collections.sort(followees, Collections.reverseOrder(new NameComparator()));
        }

        if (limit == null || limit > followees.size()) {
            limit = followeesEmail.size();
        }

        logger.info("Successful getting followees for \"{}\"", email);
        // c учетом лимита
        return followees.subList(0, limit);
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
            if (u1.getName() == null && u2.getName() == null) {
                return 0;
            }
            if (u1.getName() == null) {
                return 1;
            }
            if (u2.getName() == null) {
                return -1;
            }
            return u1.getName().compareTo(u2.getName());
        }
    }
}
