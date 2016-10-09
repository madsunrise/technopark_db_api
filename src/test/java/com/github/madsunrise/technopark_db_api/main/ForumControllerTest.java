package com.github.madsunrise.technopark_db_api.main;


import org.junit.Test;


import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ivan on 09.10.16.
 */
public class ForumControllerTest extends AbstractControllerTest {

    @Test
    public void testCreate() throws Exception {
        createUser("richard.nixon@example.com");

        mockMvc.perform(post("/db/api/forum/create/")
                .content("{\"name\": \"Forum With Sufficiently Large Name\", \"short_name\": \"forumwithsufficientlylargename\", \"user\": \"richard.nixon@example.com\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(1));

    }
}