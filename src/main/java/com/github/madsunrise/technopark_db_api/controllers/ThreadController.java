package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAO;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAODataBaseImpl;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAOImpl;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
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

    private final ThreadDAO threadDAODataBase;

    public ThreadController(ThreadDAODataBaseImpl threadDAODataBase) {
        this.threadDAODataBase = threadDAODataBase;
    }

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

        final ThreadDetails result = threadDAODataBase.create(request.forum, request.title,
                request.closed, request.user, dateTime, request.message, request.slug, request.deleted);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }

        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details(@RequestParam("thread") int threadId,
                                  @RequestParam(value = "related", required = false) List<String> related) {

        if (related != null) {
            switch (related.size()) {
                case 1: {
                    if (!related.contains("user") && !related.contains("forum")) {
                        return new CustomResponse<>(Codes.INCORRECT_REQUEST, "Bad parametres");
                    }
                    break;
                }
                case 2: {
                    if (!(related.contains("user") && related.contains("forum"))) {
                        return new CustomResponse<>(Codes.INCORRECT_REQUEST, "Bad parametres");
                    }
                    break;
                }
                default: {
                    return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
                }
            }
        }

        final ThreadDetailsExtended result = threadDAODataBase.getDetails(threadId, related);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/close", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse close(@RequestBody ThreadId request) {

        if (request.getThreadId() == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }

        final boolean success = threadDAODataBase.close(request.getThreadId());

        if (success == false) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(request.getThreadId());
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/open", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse open(@RequestBody ThreadId request) {
        if (request.getThreadId() == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }

        final boolean success = threadDAODataBase.open(request.getThreadId());

        if (success == false) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(request.getThreadId());
        return new CustomResponse<>(Codes.OK, result);
    }



    @RequestMapping(path = "/db/api/thread/remove", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse remove(@RequestBody ThreadId request) {
        final boolean success = threadDAODataBase.remove(request.getThreadId());
        if (!success) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(request.getThreadId());
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/restore", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse restore(@RequestBody ThreadId request) {
        final boolean success = threadDAODataBase.restore(request.getThreadId());
        if (!success) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread not found");
        }
        final ThreadId result = new ThreadId(request.getThreadId());
        return new CustomResponse<>(Codes.OK, result);
    }





    @RequestMapping(path = "/db/api/thread/subscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse subscribe(@RequestBody SubscribeBody request) {
        if (StringUtils.isEmpty(request.getUserEmail())) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final Long id = threadDAODataBase.subscribe(request.getThreadId(), request.getUserEmail());
        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread or user not found");
        }
        threadDAODataBase.subscribe(request.getThreadId(), request.getUserEmail());
        final SubscribeBody result = new SubscribeBody(id, request.getUserEmail());
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/unsubscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse unsubscribe(@RequestBody SubscribeBody request) {
        if (StringUtils.isEmpty(request.getUserEmail())) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final Long id = threadDAODataBase.unsubscribe(request.getThreadId(), request.getUserEmail());
        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Thread or user not found");
        }
        final SubscribeBody result = new SubscribeBody(id, request.getUserEmail());
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/thread/listPosts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse listPosts(@RequestParam("thread") Long threadId,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                    @RequestParam(value = "since", required = false) String sinceStr,
                                    @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort){

        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        final List<PostDetailsExtended> result = threadDAODataBase.getPosts(threadId, since, limit, order, sort);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Not found");
        }
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/vote", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse vote(@RequestBody VoteRequest request) {

        final ThreadDetailsExtended result = threadDAODataBase.vote(request.getThreadId(), request.getVote());
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse update(@RequestBody UpdateRequest request) {
        if (StringUtils.isEmpty(StringUtils.isEmpty(request.slug)
                || StringUtils.isEmpty(request.message))) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final ThreadDetailsExtended result = threadDAODataBase.update(request.threadId, request.message, request.slug);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/thread/list/", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse list(@RequestParam(value = "forum", required = false) String forumShortName,
                               @RequestParam(value = "user", required = false) String userEmail,
                               @RequestParam(value = "since", required = false) String sinceStr,
                               @RequestParam(value = "limit", required = false) Integer limit,
                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {

        if (StringUtils.isEmpty(forumShortName) && StringUtils.isEmpty(userEmail)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            since = LocalDateTime.parse(sinceStr, formatter);
        }

        final List<ThreadDetailsExtended> result;
        if (!StringUtils.isEmpty(forumShortName)) {
            result = threadDAODataBase.getThreadsByForum(forumShortName, since, limit, order);
        } else {
            result = threadDAODataBase.getThreadsByUser(userEmail, since, limit, order);
        }

        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "No threads");
        }

        return new CustomResponse<>(Codes.OK, result);
    }













    private static class ThreadId {
            @JsonProperty("thread")
            private Long threadId;

            @SuppressWarnings("unused")
            ThreadId() {
            }

            public ThreadId(Long threadId) {
                this.threadId = threadId;
            }

            public Long getThreadId() {
                return threadId;
            }
        }

    private static class VoteRequest {
        @JsonProperty("thread")
        private long threadId;
        @JsonProperty("vote")
        private int vote;

        @SuppressWarnings("unused")
        VoteRequest() {
        }
        public VoteRequest(long threadId, int vote) {
            this.threadId = threadId;
            this.vote = vote;
        }
        public long getThreadId() {
            return threadId;
        }
        public int getVote() {
            return vote;
        }
    }


    private static class SubscribeBody {
        @JsonProperty("thread")
        private long threadId;
        @JsonProperty("user")
        private String userEmail;

        @SuppressWarnings("unused")
        SubscribeBody() {
        }

        public SubscribeBody(long threadId) {
            this.threadId = threadId;
        }

        public SubscribeBody(long threadId, String userEmail) {
            this.threadId = threadId;
            this.userEmail = userEmail;
        }

        public long getThreadId() {
            return threadId;
        }

        public String getUserEmail() {
            return userEmail;
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


    private static class UpdateRequest {
        @JsonProperty("message")
        private String message;
        @JsonProperty("slug")
        private String slug;
        @JsonProperty("thread")
        private long threadId;

        @SuppressWarnings("unused")
        UpdateRequest() {
        }

        public UpdateRequest(String message, String slug, long threadId) {
            this.message = message;
            this.slug = slug;
            this.threadId = threadId;
        }

        public String getMessage() {
            return message;
        }

        public String getSlug() {
            return slug;
        }

        public long getThreadId() {
            return threadId;
        }
    }
}
