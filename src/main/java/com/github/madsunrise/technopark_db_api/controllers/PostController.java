package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.DAO.ForumDAO;
import com.github.madsunrise.technopark_db_api.DAO.PostDAO;
import com.github.madsunrise.technopark_db_api.DAO.ThreadDAO;
import com.github.madsunrise.technopark_db_api.response.PostDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
import com.github.madsunrise.technopark_db_api.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by ivan on 12.10.16.
 */
@RestController
public class PostController {

    private final PostDAO postDAODataBase;
    private final ForumDAO forumDAODataBase;
    private final ThreadDAO threadDAODataBase;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class.getName());

    public PostController(PostDAO postDAODataBase, ForumDAO forumDAODataBase,
                          ThreadDAO threadDAODataBase) {
        this.postDAODataBase = postDAODataBase;
        this.forumDAODataBase = forumDAODataBase;
        this.threadDAODataBase = threadDAODataBase;
    }

    @RequestMapping(path = "/db/api/post/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result create(@RequestBody CreateRequest request) {
        if (StringUtils.isEmpty(request.date) ||
                StringUtils.isEmpty(request.message) || StringUtils.isEmpty(request.user)
                || StringUtils.isEmpty(request.forum)) {
            return Result.badRequest();
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final LocalDateTime date = LocalDateTime.parse(request.date, formatter);

        PostDetails<String, String, String> result;

            try {

                result = postDAODataBase.create(date, request.thread, request.message,
                        request.user, request.forum, request.parent, request.approved, request.highlighted, request.edited,
                        request.spam, request.deleted);

            } catch (DeadlockLoserDataAccessException e) {
                LOGGER.error("Deadlock!");
                return Result.badRequest();

        }

        if (result == null) {
            return Result.badRequest();
        }

        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/post/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result details(@RequestParam("post") int postId,
                          @RequestParam(value = "related", required = false) List<String> array) {
        final PostDetailsExtended result = postDAODataBase.getDetails(postId, array);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/post/list/", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Result list(@RequestParam(value = "forum", required = false) String forumShortName,
                       @RequestParam(value = "thread", required = false) Long threadId,
                       @RequestParam(value = "since", required = false) String sinceStr,
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {

        if (StringUtils.isEmpty(forumShortName) && threadId == null) {
            return Result.badRequest();
        }

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            since = LocalDateTime.parse(sinceStr, formatter);
        }


        final List<PostDetailsExtended> result;
        if (forumShortName != null) {
            result = forumDAODataBase.getPosts(forumShortName, since, limit, order, null);
        } else {
            result = threadDAODataBase.getPosts(threadId, since, limit, order, "flat");
        }

        if (result == null) {
            return Result.notFound();
        }

        return Result.ok(result);
    }


    @RequestMapping(path = "/db/api/post/remove", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result remove(@RequestBody PostId postId) {
        final boolean success = postDAODataBase.remove(postId.getPostId());
        if (!success) {
            return Result.notFound();
        }
        final PostId result = new PostId(postId.getPostId());
        return Result.ok(result);
    }

    @RequestMapping(path = "/db/api/post/restore", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result restore(@RequestBody PostId postId) {
        final boolean success = postDAODataBase.restore(postId.getPostId());
        if (!success) {
            return Result.notFound();
        }
        final PostId result = new PostId(postId.getPostId());
        return Result.ok(result);
    }

    @RequestMapping(path = "/db/api/post/vote", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result vote(@RequestBody VoteRequest request) {

        final PostDetailsExtended result = postDAODataBase.vote(request.getPostId(), request.getVote());
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }

    @RequestMapping(path = "/db/api/post/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Result update(@RequestBody UpdateRequest request) {
        if (StringUtils.isEmpty(StringUtils.isEmpty(request.message))) {
            return Result.badRequest();
        }

        final PostDetailsExtended result = postDAODataBase.update(request.postId, request.message);
        if (result == null) {
            return Result.notFound();
        }
        return Result.ok(result);
    }




    private static class PostId {
        @JsonProperty("post")
        private long postId;

        @SuppressWarnings("unused")
        PostId() {
        }

        public PostId(long postId) {
            this.postId = postId;
        }

        public long getPostId() {
            return postId;
        }
    }


    private static class VoteRequest {
        @JsonProperty("post")
        private long postId;
        @JsonProperty("vote")
        private int vote;

        @SuppressWarnings("unused")
        VoteRequest() {
        }

        public VoteRequest(long postId, int vote) {
            this.postId = postId;
            this.vote = vote;
        }

        public long getPostId() {
            return postId;
        }

        public int getVote() {
            return vote;
        }
    }


    private static class CreateRequest {
        @JsonProperty("date")
        private String date;
        @JsonProperty("message")
        private String message;
        @JsonProperty("thread")
        private long thread;
        @JsonProperty("user")
        private String user;
        @JsonProperty("forum")
        private String forum;

        @JsonProperty(value = "parent", required = false)
        private Long parent;
        @JsonProperty(value = "isApproved", required = false)
        private boolean approved;
        @JsonProperty(value = "isHighlighted", required = false)
        private boolean highlighted;
        @JsonProperty(value = "isEdited", required = false)
        private boolean edited;
        @JsonProperty(value = "isSpam", required = false)
        private boolean spam;
        @JsonProperty(value = "isDeleted", required = false)
        private boolean deleted;

        @SuppressWarnings("unused")
        CreateRequest() {
        }

        CreateRequest(String date, String message, long thread, String user, String forum, Long parent,
                      boolean approved, boolean highlighted, boolean edited, boolean spam, boolean deleted) {
            this.date = date;
            this.message = message;
            this.thread = thread;
            this.user = user;
            this.forum = forum;
            this.parent = parent;
            this.approved = approved;
            this.highlighted = highlighted;
            this.edited = edited;
            this.spam = spam;
            this.deleted = deleted;
        }
    }

    private static class UpdateRequest {
        @JsonProperty("message")
        private String message;
        @JsonProperty("post")
        private long postId;

        @SuppressWarnings("unused")
        UpdateRequest() {
        }

        public UpdateRequest(String message, long postId) {
            this.message = message;
            this.postId = postId;
        }

        public String getMessage() {
            return message;
        }

        public long getPostId() {
            return postId;
        }
    }
}
