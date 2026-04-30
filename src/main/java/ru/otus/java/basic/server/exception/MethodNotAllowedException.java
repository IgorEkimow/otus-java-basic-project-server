package ru.otus.java.basic.server.exception;

import ru.otus.java.basic.server.http.HttpStatus;

public class MethodNotAllowedException extends ServerException {
    public MethodNotAllowedException(String message) {
        super(HttpStatus.METHOD_NOT_ALLOWED, message);
    }
}