package com.github.madsunrise.technopark_db_api.response;

/**
 * Created by ivan on 08.10.16.
 */
public final class CustomResponse {
        private final int code;
        private final String reason;

        private CustomResponse(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }

    @SuppressWarnings("unused")
    public int getCode() {
        return code;
    }

    @SuppressWarnings("unused")
    public String getReason() {
        return reason;
    }
}
