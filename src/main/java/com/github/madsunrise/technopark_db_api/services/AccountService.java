package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.model.UserProfile;

/**
 * Created by ivan on 05.10.16.
 */
public interface AccountService {
    UserProfile addUser(String login, String password, String email);
    UserProfile getUserByLogin(String login);
    UserProfile getUserById(Long id);
}
