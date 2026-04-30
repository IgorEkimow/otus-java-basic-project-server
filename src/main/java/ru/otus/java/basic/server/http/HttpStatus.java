package ru.otus.java.basic.server.http;

public record HttpStatus(int code, String reason) {
    public static final HttpStatus OK = new HttpStatus(200, "OK");
    public static final HttpStatus CREATED = new HttpStatus(201, "Created");
    public static final HttpStatus NO_CONTENT = new HttpStatus(204, "No Content");
    public static final HttpStatus BAD_REQUEST = new HttpStatus(400, "Bad Request");
    public static final HttpStatus NOT_FOUND = new HttpStatus(404, "Not Found");
    public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(405, "Method Not Allowed");
    public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(406, "Not Acceptable");
    public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(500, "Internal Server Error");
    public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(501, "Not Implemented");
}