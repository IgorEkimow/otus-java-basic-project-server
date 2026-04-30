package ru.otus.java.basic.server.exception;

import ru.otus.java.basic.server.http.HttpStatus;

public class BadRequestException extends ServerException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}