package ru.otus.java.basic.server.exception;

import ru.otus.java.basic.server.http.HttpStatus;

public class NotFoundException extends ServerException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}