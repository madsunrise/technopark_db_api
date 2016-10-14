package com.github.madsunrise.technopark_db_api.controllers;

import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.*;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.response.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ivan on 13.10.16.
 */
@RestController
public class MainController {
    private final UserDAO userDAO = new UserDAOImpl();
    private final ForumDAO forumDAO = new ForumDAOImpl();
    private final ThreadDAO threadDAO = new ThreadDAOImpl();
    private final PostDAO postDAO = new PostDAOImpl();


    @RequestMapping(path = "/db/api/clear", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details() {
        postDAO.clear();
        threadDAO.clear();
        forumDAO.clear();
        userDAO.clear();
        return new CustomResponse<>(Codes.OK, "OK");
    }

    @RequestMapping(path = "/db/api/status", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse status() {
        long postCount = postDAO.getAmount();
        long threadCount = threadDAO.getAmount();
        long forumCount = forumDAO.getAmount();
        long userCount = userDAO.getAmount();
        StatusResponse result = new StatusResponse(userCount, threadCount, forumCount, postCount);
        return new CustomResponse<>(Codes.OK, result);
    }
}
