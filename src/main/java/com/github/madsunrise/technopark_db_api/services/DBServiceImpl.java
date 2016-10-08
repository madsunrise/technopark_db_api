package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.Config;
import com.github.madsunrise.technopark_db_api.DAO.UserDAO;
import com.github.madsunrise.technopark_db_api.DAO.UserDAOImpl;
import com.github.madsunrise.technopark_db_api.builder.ForumBuilder;
import com.github.madsunrise.technopark_db_api.builder.PostBuilder;
import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.model.User;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ivan on 08.10.16.
 */
@Service
public class DBServiceImpl implements DBService {
    private Connection connection = null;
    private UserDAO userDAO;


    DBServiceImpl() throws SQLException{
        try {
            Class.forName(Config.SQL_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your JDBC Driver?");
            e.printStackTrace();
            return;
        }

        System.out.println("MySQL JDBC Driver Registered!");
        connection = DriverManager
                    .getConnection(Config.DATABASE_URL,Config.DATABASE_USER, Config.DATABASE_PASSWORD);


        userDAO = new UserDAOImpl(connection);
    }

    @Override
    public CustomResponse clear() {
        return null;
    }

    @Override
    public CustomResponse status() {
        return null;
    }

    @Override
    public CustomResponse createForum(String name, String shortName, String userEmail) {
        User user = userDAO.getByEmail(userEmail);
        Forum forum = new ForumBuilder(name, shortName, userEmail, user.getId()).build();
        return new CustomResponse(0, "r");
    }

    @Override
    public CustomResponse getForumDetails(String shortName) {
        return null;
    }

    @Override
    public CustomResponse getForumDetails(String shortName, List<String> related) {
        return null;
    }

    @Override
    public CustomResponse getForumPosts(String shortName, String since, int limit, String order, List<String> related) {
        return null;
    }

    @Override
    public CustomResponse getForumThreads(String shortName, String since, int limit, String order, List<String> related) {
        return null;
    }

    @Override
    public CustomResponse getForumUsers(String shortName, int limit, String order, int sinceId) {
        return null;
    }

    @Override
    public PostBuilder createPost(String message, String date, int threadId, String user, long userId, String forum, long forumId) {
        return null;
    }

    @Override
    public CustomResponse getPostsList(String forum) {
        return null;
    }

    @Override
    public CustomResponse getPostsList(int threadId) {
        return null;
    }
}
