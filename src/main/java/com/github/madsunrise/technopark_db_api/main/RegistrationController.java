package com.github.madsunrise.technopark_db_api.main;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.github.madsunrise.technopark_db_api.services.AccountServiceImpl;

import javax.servlet.http.HttpSession;


@RestController
public class RegistrationController {

    private final AccountServiceImpl accountService;

    @Autowired
    public RegistrationController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }


    @RequestMapping(path = "/api/signup", method = RequestMethod.POST)
    public ResponseEntity signup(@RequestBody RequestUser jsonString, HttpSession httpSession) {



    }





    private static final class SuccessSignupResponse {
        private final String login;
        private final String email;

        private SuccessSignupResponse(String login, String email) {
            this.login = login;
            this.email = email;
        }

        @SuppressWarnings("unused")
        public String getLogin() {
            return login;
        }

        @SuppressWarnings("unused")
        public String getEmail() {
            return email;
        }
    }



    public static final class ErrorResponse {
        private final HttpStatus code;
        private final String reason;

        public ErrorResponse(HttpStatus code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        @SuppressWarnings("unused")
        public int getCode() {
            return code.value();
        }

        @SuppressWarnings("unused")
        public String getReason() {
            return reason;
        }
    }

public static class RequestUser {
    private String login;
    private String password;
    private String email;

    @SuppressWarnings("unused")
    public RequestUser() {

    }

    @SuppressWarnings("unused")
    public RequestUser(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
}
