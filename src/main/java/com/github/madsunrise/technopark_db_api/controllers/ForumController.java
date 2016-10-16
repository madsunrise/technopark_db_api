package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.ForumDAO;
import com.github.madsunrise.technopark_db_api.DAO.ForumDAOImpl;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
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
    private final ForumDAO forumDAO = new ForumDAOImpl();


    @RequestMapping(path = "/db/api/forum/create", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse create(@RequestBody CreateRequest request) {
        if (StringUtils.isEmpty(request.name) ||
                StringUtils.isEmpty(request.shortName) || StringUtils.isEmpty(request.user)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final ForumDetails result = forumDAO.create(request.name, request.shortName, request.user);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "User not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/forum/details", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details(@RequestParam(value = "forum") String shortName,
                                  @RequestParam(value = "related", required = false) String related) {
        if (StringUtils.isEmpty(shortName)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final ForumDetails result = forumDAO.getDetails(shortName, related);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Forum not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }



    @RequestMapping(path = "/db/api/forum/listPosts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse listPosts(@RequestParam("forum") String forumShortName,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                    @RequestParam(value = "since", required = false) String sinceStr,
                                    @RequestParam(value = "related", required = false) List<String> related){

        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        final List<PostDetailsExtended> result = forumDAO.getPosts(forumShortName, since, limit, order, related);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Not found");
        }
        return new CustomResponse<>(Codes.OK, result);
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

