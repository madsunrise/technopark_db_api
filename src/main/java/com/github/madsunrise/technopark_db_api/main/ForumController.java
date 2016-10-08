package com.github.madsunrise.technopark_db_api.main;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.services.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ivan on 08.10.16.
 */
@RestController
public class ForumController {
    private final DBService dbService;
    Logger logger = LoggerFactory.getLogger(ForumController.class.getName());

    @Autowired
    public ForumController(DBService dbService) {
        this.dbService = dbService;
    }

    @RequestMapping(path = "/db/api/forum/create", method = RequestMethod.POST)
    @ResponseBody
    public CustomResponse create(@RequestBody CreateRequest request) {
       return dbService.createForum(request.name, request.shortName, request.user);
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

