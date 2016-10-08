package com.github.madsunrise.technopark_db_api.services;

import com.github.madsunrise.technopark_db_api.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountServiceImpl implements AccountService {
    private final Map<String, UserProfile> loginToProfile = new ConcurrentHashMap<>();
    private final Map<Long, UserProfile> idToProfile = new ConcurrentHashMap<>();

    @Override
    public UserProfile addUser(String login, String password, String email) {
        final UserProfile userProfile = new UserProfile(login, email, password);
        loginToProfile.put(login, userProfile);
        idToProfile.put(userProfile.getId(), userProfile);
        return userProfile;
    }

    @Override
    public UserProfile getUserByLogin(String login) {
        return loginToProfile.get(login);
    }

    @Override
    public UserProfile getUserById(Long id) {
        return idToProfile.get(id);
    }
}
