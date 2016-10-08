package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.model.User;

/**
 * Created by ivan on 05.10.16.
 */
public interface AccountService {
    User addUser(String login, String password, String email);
    User getUserByLogin(String login);
    User getUserById(Long id);
}
