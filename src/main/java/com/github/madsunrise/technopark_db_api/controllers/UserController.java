package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.UserDAO;
import com.github.madsunrise.technopark_db_api.DAO.UserDAOImpl;
import com.github.madsunrise.technopark_db_api.response.UserDetails;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;

import com.github.madsunrise.technopark_db_api.response.UserDetailsExtended;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ivan on 11.10.16.
 */

@RestController
public class UserController {
    private final UserDAO userDAO = new UserDAOImpl();


    @RequestMapping(path = "/db/api/user/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse create(@RequestBody CreateUser request) {
        if (StringUtils.isEmpty(request.email)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final UserDetails result = userDAO.create(request.username,
                request.name, request.email, request.about, request.anonymous);
        if (result == null) {
            return new CustomResponse<>(Codes.USER_EXISTS, "User exists");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/user/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details(@RequestParam("user") String email) {
        final UserDetailsExtended result = userDAO.getDetails(email);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "User not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/user/updateProfile", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse updateProfile(@RequestBody UpdateUser request) {
        if (StringUtils.isEmpty(request.name) || StringUtils.isEmpty(request.email)
                || StringUtils.isEmpty(request.about)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final UserDetailsExtended result = userDAO.updateProfile(request.email, request.name, request.about);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "User not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }




    @RequestMapping(path = "/db/api/user/follow", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse follow(@RequestBody FollowRequest request) {
        if (StringUtils.isEmpty(request.follower) || StringUtils.isEmpty(request.followee)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final UserDetailsExtended result = userDAO.follow(request.follower, request.followee);
        if (result == null) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/user/unfollow", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse unfollow(@RequestBody FollowRequest request) {
        if (StringUtils.isEmpty(request.follower) || StringUtils.isEmpty(request.followee)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final UserDetailsExtended result = userDAO.unfollow(request.follower, request.followee);
        if (result == null) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }




    @RequestMapping(path = "/db/api/user/listFollowers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse listFollowers(@RequestParam("user") String email,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam(value = "since_id", required = false) Integer sinceId){
        final UserDetailsExtended result = userDAO.getFollowers(email, limit, order, sinceId);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "User not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/user/listFollowing", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse listFollowing(@RequestParam("user") String email,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam(value = "since_id", required = false) Integer sinceId){
        final UserDetailsExtended result = userDAO.getFollowing(email, limit, order, sinceId);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "User not found");
        }
        return new CustomResponse<>(Codes.OK, result);
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