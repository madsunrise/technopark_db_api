package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAO;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAOImpl;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.response.ThreadDetails;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;
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
public class ThreadController {
    private final ThreadDAO threadDAO = new ThreadDAOImpl();


    @RequestMapping(path = "/db/api/thread/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse create(@RequestBody CreateRequest request) {
        if (StringUtils.isEmpty(request.title) ||
                StringUtils.isEmpty(request.message) || StringUtils.isEmpty(request.forum) ||
                StringUtils.isEmpty(request.date) || StringUtils.isEmpty(request.slug)
                || StringUtils.isEmpty(request.user)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final LocalDateTime dateTime = LocalDateTime.parse(request.date, formatter);

        final ThreadDetails result = threadDAO.create(request.forum, request.title,
                request.closed, request.user, dateTime, request.message, request.slug, request.deleted);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details(@RequestParam("thread") int threadId,
                                  @RequestParam(value = "related", required = false) List<String> array) {

        if (array != null) {
            switch (array.size()) {
                case 1: {
                    if (!array.contains("user") && !array.contains("forum")) {
                        return new CustomResponse<>(Codes.INCORRECT_REQUEST, "Bad parametres");
                    }
                    break;
                }
                case 2: {
                    if (!(array.contains("user") && array.contains("forum"))) {
                        return new CustomResponse<>(Codes.INCORRECT_REQUEST, "Bad parametres");
                    }
                    break;
                }
                default: {
                    return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
                }
            }
        }

        final ThreadDetailsExtended result = threadDAO.getDetails(threadId, array);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/close", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse close(@RequestBody ThreadId request) {
        final Long id = threadDAO.close(request.getThreadId());

        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(id);
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/remove", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse remove(@RequestBody ThreadId request) {
        final Long id = threadDAO.remove(request.getThreadId());
        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(id);
        return new CustomResponse<>(Codes.OK, result);
    }













    private static class ThreadId {
            @JsonProperty("thread")
            private long threadId;

            @SuppressWarnings("unused")
            ThreadId() {
            }

            public ThreadId(long threadId) {
                this.threadId = threadId;
            }

            public long getThreadId() {
                return threadId;
            }
        }


    private static class CreateRequest {
        @JsonProperty("title")
        private String title;
        @JsonProperty("message")
        private String message;
        @JsonProperty("user")
        private String user;
        @JsonProperty("forum")
        private String forum;
        @JsonProperty("date")
        private String date;
        @JsonProperty("slug")
        private String slug;
        @JsonProperty("isClosed")
        private boolean closed;
        @JsonProperty(value = "isDeleted", required = false)
        private boolean deleted;

        @SuppressWarnings("unused")
        CreateRequest() {
        }

        @SuppressWarnings("unused")
        CreateRequest(String title, String message, String user,
                             String forum, String date, String slug, boolean closed, boolean deleted) {
            this.title = title;
            this.message = message;
            this.user = user;
            this.forum = forum;
            this.date = date;
            this.slug = slug;
            this.closed = closed;
            this.deleted = deleted;
        }
    }
}
