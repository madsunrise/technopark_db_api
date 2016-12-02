package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.DAO.ForumDAOImpl;
import com.github.madsunrise.technopark_db_api.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by ivan on 08.10.16.
 */
@RestController
public class ForumController {
    private final ForumDAOImpl forumDAODataBase;

    public ForumController(ForumDAOImpl forumDAODataBase) {
        this.forumDAODataBase = forumDAODataBase;
    }


    @RequestMapping(path = "/db/api/forum/create", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Result create(@RequestBody CreateRequest request) {
        if (StringUtils.isEmpty(request.name) ||
                StringUtils.isEmpty(request.shortName) || StringUtils.isEmpty(request.user)) {
            return Result.badRequest();
        }
        final ForumDetails result = forumDAODataBase.create(request.name, request.shortName, request.user);
        if (result == null) {
            return Result.notFound();
        }
        //forumDAO.create(request.name, request.shortName, request.user);
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/forum/details", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Result details(@RequestParam(value = "forum") String shortName,
                          @RequestParam(value = "related", required = false) List<String> related) {
        if (StringUtils.isEmpty(shortName)) {
            return Result.badRequest();
        }

        final ForumDetails result = forumDAODataBase.getDetails(shortName, related);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }



    @RequestMapping(path = "/db/api/forum/listPosts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listPosts(@RequestParam("forum") String forumShortName,
                            @RequestParam(value = "limit", required = false) Integer limit,
                            @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                            @RequestParam(value = "since", required = false) String sinceStr,
                            @RequestParam(value = "related", required = false) List<String> related){

        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        final List<PostDetailsExtended> result = forumDAODataBase.getPosts(forumShortName, since, limit, order, related);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }

    @RequestMapping(path = "/db/api/forum/listThreads", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listThreads(@RequestParam("forum") String forumShortName,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                              @RequestParam(value = "since", required = false) String sinceStr,
                              @RequestParam(value = "related", required = false) List<String> related){

        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        // Надо ли?
        if (related != null && related.contains("thread")) {
            return Result.ok("\'thread\' GET-parametr...?");
        }

        final List<ThreadDetailsExtended> result = forumDAODataBase.getThreads(forumShortName, since, limit, order, related);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/forum/listUsers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result listUsers(@RequestParam("forum") String forumShortName,
                            @RequestParam(value = "limit", required = false) Integer limit,
                            @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                            @RequestParam(value = "since_id", required = false) Long sinceId){


        final List<UserDetailsExtended> result = forumDAODataBase.getUsers(forumShortName, sinceId, limit, order);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }



    @RequestMapping(path = "/db/api/forum/forumuser", method = RequestMethod.GET)
    public void script(){

//        forumDAODataBase.createUserForumTable();
//        forumDAODataBase.fillUserForumTable();
    }






    private static class CreateRequest {
        @JsonProperty("name")
        private String name;
        @JsonProperty("short_name")
        private String shortName;
        @JsonProperty("user")
        private String user;

        @SuppressWarnings("unused")
        CreateRequest() {
        }

        @SuppressWarnings("unused")
        CreateRequest(String name, String shortName, String user) {
            this.name = name;
            this.shortName = shortName;
            this.user = user;
        }
    }
}

