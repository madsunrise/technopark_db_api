package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.DAO.UserDAO;
import com.github.madsunrise.technopark_db_api.DAO.UserDAODataBaseImpl;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.Result;

import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by ivan on 11.10.16.
 */

@RestController
public class UserController {
    
    private final UserDAO userDAODataBase;

    public UserController(UserDAODataBaseImpl userDAODataBase) {
        this.userDAODataBase = userDAODataBase;
    }

    @RequestMapping(path = "/db/api/user/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result create(@RequestBody CreateUser request) {
        if (StringUtils.isEmpty(request.email)) {
            return Result.badRequest();
        }

        final UserDetails result = userDAODataBase.create(request.username,
                request.name, request.email, request.about, request.anonymous);

        if (result == null) {
            return new Result<>(Result.USER_ALREADY_EXISTS, "User exists");
        }

        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/user/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result details(@RequestParam("user") String email) {
        final UserDetailsExtended result = userDAODataBase.getDetails(email);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/user/updateProfile", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result updateProfile(@RequestBody UpdateUser request) {
        if (StringUtils.isEmpty(request.name) || StringUtils.isEmpty(request.email)
                || StringUtils.isEmpty(request.about)) {
            return Result.badRequest();
        }

        final UserDetailsExtended result = userDAODataBase.updateProfile(request.email, request.name, request.about);

        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }




    @RequestMapping(path = "/db/api/user/follow", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result follow(@RequestBody FollowRequest request) {
        if (StringUtils.isEmpty(request.follower) || StringUtils.isEmpty(request.followee)) {
            return Result.badRequest();
        }

        final UserDetailsExtended result = userDAODataBase.follow(request.follower, request.followee);
        if (result == null) {
            return Result.badRequest();
        }
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/user/unfollow", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result unfollow(@RequestBody FollowRequest request) {
        if (StringUtils.isEmpty(request.follower) || StringUtils.isEmpty(request.followee)) {
            return Result.badRequest();
        }

        final UserDetailsExtended result = userDAODataBase.unfollow(request.follower, request.followee);
        if (result == null) {
            return Result.badRequest();
        }
        return Result.ok(result);
    }




    @RequestMapping(path = "/db/api/user/listFollowers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listFollowers(@RequestParam("user") String email,
                                @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                @RequestParam(value = "since_id", required = false) Integer sinceId){
        final List<UserDetailsExtended> result;
        if (sinceId == null) {
            if (limit == null) {
                result = userDAODataBase.getFollowers(email, order);
            }
            else {
                result = userDAODataBase.getFollowers(email, order, limit);
            }
        }
        else {
            if (limit == null) {
                result = userDAODataBase.getFollowers(email, sinceId, order);
            }
            else {
                result = userDAODataBase.getFollowers(email, sinceId, order, limit);
            }
        }
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/user/listFollowing", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listFollowing(@RequestParam("user") String email,
                                @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                @RequestParam(value = "since_id", required = false) Integer sinceId){
        final List<UserDetailsExtended> result;
        if (sinceId == null) {
            if (limit == null) {
                result = userDAODataBase.getFollowees(email, order);
            }
            else {
                result = userDAODataBase.getFollowees(email, order, limit);
            }
        }
        else {
            if (limit == null) {
                result = userDAODataBase.getFollowees(email, sinceId, order);
            }
            else {
                result = userDAODataBase.getFollowees(email, sinceId, order, limit);
            }
        }
        if (result == null) {
            return Result.notFound();

        }
        return Result.ok(result);
    }





    @RequestMapping(path = "/db/api/user/listPosts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listPosts(@RequestParam("user") String email,
                            @RequestParam(value = "limit", required = false) Integer limit,
                            @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                            @RequestParam(value = "since", required = false) String sinceStr){

        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        final List<PostDetailsExtended> result = userDAODataBase.getPosts(email, since, limit, order);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }













    public static class CreateUser {
        @JsonProperty("name")
        private String name;
        @JsonProperty("username")
        private String username;
        @JsonProperty("about")
        private String about;
        @JsonProperty("email")
        private String email;
        @JsonProperty(value = "isAnonymous", required = false)
        private boolean anonymous;

        public CreateUser() {
        }

        public CreateUser(String name, String username, String about, String email, boolean anonymous) {
            this.name = name;
            this.username = username;
            this.about = about;
            this.email = email;
            this.anonymous = anonymous;
        }
    }


    public static class UpdateUser {
        @JsonProperty("name")
        private String name;
        @JsonProperty("user")
        private String email;
        @JsonProperty("about")
        private String about;

        public UpdateUser() {
        }

        public UpdateUser(String name, String email, String about) {
            this.name = name;
            this.email = email;
            this.about = about;
        }
    }

    public static class FollowRequest {
        @JsonProperty("follower")
        private String follower;
        @JsonProperty("followee")
        private String followee;

        public FollowRequest() {
        }

        public FollowRequest(String follower, String followee) {
            this.follower = follower;
            this.followee = followee;
        }
    }
}