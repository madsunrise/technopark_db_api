package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.User;

/**
 * Created by ivan on 08.10.16.
 */
public interface UserDAO {
    User getById (long id);
    User getByEmail (String email);
    int add (User user);
}
