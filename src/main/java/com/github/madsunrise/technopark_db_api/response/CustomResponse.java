package com.github.madsunrise.technopark_db_api.response;


/**
 * Created by ivan on 08.10.16.
 */
public final class CustomResponse<V> {
    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int INCORRECT_REQUEST = 3;
    public static final int UNKNOWN_ERROR = 4;
    public static final int USER_ALREADY_EXISTS = 5;


        private final int code;
        private final V response;

    public CustomResponse(int code, V response) {
        this.code = code;
        this.response = response;
    }

    @SuppressWarnings("unused")
    public int getCode() {
        return code;
    }

    @SuppressWarnings("unused")
    public V getResponse() {
        return response;
    }

    public static <V> CustomResponse<V> ok(V response) {
        return new CustomResponse<V>(OK, response);
    }

    public static CustomResponse<String> notFound() {
        return new CustomResponse<>(NOT_FOUND, "Not found");
    }
}
