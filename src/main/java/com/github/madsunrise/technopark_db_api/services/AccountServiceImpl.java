package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountServiceImpl implements AccountService {
    private final Map<String, User> loginToProfile = new ConcurrentHashMap<>();
    private final Map<Long, User> idToProfile = new ConcurrentHashMap<>();

    @Override
    public User addUser(String login, String password, String email) {
        final User user = new User(login, email, password);
        loginToProfile.put(login, user);
        idToProfile.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserByLogin(String login) {
        return loginToProfile.get(login);
    }

    @Override
    public User getUserById(Long id) {
        return idToProfile.get(id);
    }
}
