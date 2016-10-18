package com.github.madsunrise.technopark_db_api.controllers;


import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ivan on 09.10.16.
 */
public class ForumControllerTest extends AbstractControllerTest {
    protected String defaultShortName = "forumWithSufficientlyLargeName";



    @Test
    public void forumCreate() throws Exception {
        createUser("richard.nixon@example.com");
        mockMvc.perform(post("/db/api/forum/create/")
                .content("{\"name\": \"Forum With Sufficiently Large Name\", " +
                        "\"short_name\": \"" + defaultShortName + "\", \"user\": \"richard.nixon@example.com\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(0))
                .andExpect(jsonPath("response.user").value("richard.nixon@example.com"));

    }

    @Test
    public void testDetails() throws Exception{
        forumCreate();
        mockMvc.perform(get("/db/api/forum/details")
                .param("forum", "forumwithsufficientlylargename")
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(0));

        mockMvc.perform(get("/db/api/forum/details")
                .param("forum", "forumwithsufficientlylargename")
                .param("related", "user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(0))
                .andExpect(jsonPath("response.user.name").value("John"))
                .andExpect(jsonPath("response.user.email").value("richard.nixon@example.com"));
    }
}