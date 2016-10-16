package com.github.madsunrise.technopark_db_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.madsunrise.technopark_db_api.Codes;
import com.github.madsunrise.technopark_db_api.DAO.PostDAO;
import com.github.madsunrise.technopark_db_api.DAO.PostDAOImpl;
import com.github.madsunrise.technopark_db_api.response.CustomResponse;
import com.github.madsunrise.technopark_db_api.response.PostDetails;
import com.github.madsunrise.technopark_db_api.response.PostDetailsExtended;
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
    final private PostDAO postDAO = new PostDAOImpl();

    @RequestMapping(path = "/db/api/post/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse create(@RequestBody CreateRequest request) {
        if (StringUtils.isEmpty(request.date) ||
                StringUtils.isEmpty(request.message) || StringUtils.isEmpty(request.user)
                || StringUtils.isEmpty(request.forum)) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final LocalDateTime date = LocalDateTime.parse(request.date, formatter);
        final PostDetails<String, String, String> result = postDAO.create(date, request.thread, request.message,
                request.user, request.forum, request.parent, request.approved, request.highlighted, request.edited,
                request.spam, request.deleted);
        if (result == null) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parameters");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/post/details", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse details(@RequestParam("post") int postId,
                                  @RequestParam(value = "related", required = false) List<String> array) {
        final PostDetailsExtended result = postDAO.getDetails(postId, array);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/post/list/", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse list(@RequestParam(value = "forum", required = false) String forumShortName,
                               @RequestParam(value = "thread", required = false) Long threadId,
                               @RequestParam(value = "since", required = false) String sinceStr,
                               @RequestParam(value = "limit", required = false) Integer limit,
                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {

        if (StringUtils.isEmpty(forumShortName) && threadId == null) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime since = null;
        if (!StringUtils.isEmpty(sinceStr)) {
            since = LocalDateTime.parse(sinceStr, formatter);
        }


        final List<PostDetailsExtended> result;
        if (forumShortName != null) {
            result = postDAO.getPostsByForum(forumShortName, since, limit, order);
        } else {
            result = postDAO.getPostsByThread(threadId, since, limit, order);
        }

        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "No posts");
        }

        return new CustomResponse<>(Codes.OK, result);
    }


    @RequestMapping(path = "/db/api/post/remove", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse remove(@RequestBody PostId postId) {
        final Long id = postDAO.remove(postId.getPostId());
        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Post not found");
        }
        final PostId result = new PostId(id);
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/post/restore", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse restore(@RequestBody PostId postId) {
        final Long id = postDAO.restore(postId.getPostId());
        if (id == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Post not found");
        }
        final PostId result = new PostId(id);
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/post/vote", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse vote(@RequestBody VoteRequest request) {

        final PostDetailsExtended result = postDAO.vote(request.getPostId(), request.getVote());
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Bad parametres");
        }
        return new CustomResponse<>(Codes.OK, result);
    }

    @RequestMapping(path = "/db/api/post/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse update(@RequestBody UpdateRequest request) {
        if (StringUtils.isEmpty(StringUtils.isEmpty(request.message))) {
            return new CustomResponse<>(Codes.INVALID_REQUEST, "Bad parametres");
        }

        final PostDetailsExtended result = postDAO.update(request.postId, request.message);
        if (result == null) {
            return new CustomResponse<>(Codes.NOT_FOUND, "Post not found");
        }
        return new CustomResponse<>(Codes.OK, result);
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
