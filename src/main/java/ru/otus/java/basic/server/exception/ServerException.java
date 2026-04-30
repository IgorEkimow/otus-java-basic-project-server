package ru.otus.java.basic.server.exception;

import lombok.Getter;
import ru.otus.java.basic.server.http.HttpStatus;

@Getter
public class ServerException extends RuntimeException {
    private final HttpStatus status;

    public ServerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ServerException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}