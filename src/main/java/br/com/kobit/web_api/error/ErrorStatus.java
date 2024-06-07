package br.com.kobit.web_api.error;

import java.io.Serializable;

import com.google.gson.Gson;

public class ErrorStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final String exception;
    private final String cause;

    public ErrorStatus(String message, String exception, String cause) {
        this.message = message;
        this.exception = exception;
        this.cause = cause;
    }

    public ErrorStatus(Exception exception) {
        this.message = exception.getMessage();
        this.exception = String.valueOf(exception);
        this.cause = String.valueOf(exception.getCause());
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    public String getCause() {
        return cause;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
