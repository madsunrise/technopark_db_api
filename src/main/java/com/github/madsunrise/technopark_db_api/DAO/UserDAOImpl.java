package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;

import java.sql.Connection;

/**
 * Created by ivan on 08.10.16.
 */
public class UserDAOImpl implements UserDAO {
    Connection connection;
    public UserDAOImpl (Connection connection) {
        this.connection = connection;
    }

    @Override
    public User getById(long id) {
        return null;
    }

    @Override
    public User getByEmail(String email) {
        return null;
    }

    @Override
    public int add(User user) {
        return 0;
    }
}
