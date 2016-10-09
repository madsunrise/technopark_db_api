package com.github.madsunrise.technopark_db_api.main;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


/**
 * Created by ivan on 09.10.16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
//@Transactional
public abstract class AbstractControllerTest {
    @Autowired
    protected MockMvc mockMvc;


    protected ResultActions createUser() throws Exception {

        return createUser("example@mail.ru");
    }

    protected ResultActions createUser(String email) throws Exception {
        return createUser(email, true);
    }

    protected ResultActions createUser(String email, boolean withAnonymous) throws Exception {
        String user = "{\"username\": \"user1\", \"about\": \"hello im user1\",";
        if (withAnonymous) user += " \"isAnonymous\": false,";
        user += " \"name\": \"John\", \"email\": \"" + email + "\"}";

        return mockMvc.perform(post("/db/api/user/create/")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON));
    }

}
