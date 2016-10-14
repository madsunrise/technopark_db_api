package com.github.madsunrise.technopark_db_api.controllers;

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ivan on 11.10.16.
 */
public class ThreadControllerTest extends ForumControllerTest {

    protected String emailOne = "example@mail.ru";

    @Test
    public void threadCreate() throws Exception {
        forumCreate();
        createUser(emailOne);

        mockMvc.perform(post("/db/api/thread/create/")
                .content("{\"forum\": \"" + defaultShortName + "\", \"title\": \"Thread With Sufficiently Large Title\"," +
                                " \"isClosed\": true, \"user\": \"" + emailOne + "\", \"date\": \"2014-01-01 00:00:01\"," +
                        "\"message\": \"hey hey hey hey!\", \"slug\": \"Threadwithsufficientlylargetitle\", \"isDeleted\": true}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(0))
                .andExpect(jsonPath("response.user").value(emailOne));
    }
}
