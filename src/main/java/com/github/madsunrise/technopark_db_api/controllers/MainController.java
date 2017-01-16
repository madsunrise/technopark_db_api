package com.github.madsunrise.technopark_db_api.controllers;

import com.github.madsunrise.technopark_db_api.DAO.MainService;
import com.github.madsunrise.technopark_db_api.response.Result;
import com.github.madsunrise.technopark_db_api.response.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ivan on 13.10.16.
 */
@RestController
public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }



    @RequestMapping(path = "/db/api/clear", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Result details() {
        mainService.clear();
        return Result.ok("Cleared");
    }

    @RequestMapping(path = "/db/api/status", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Result status() {
        return Result.ok(mainService.getStatus());
    }
}
