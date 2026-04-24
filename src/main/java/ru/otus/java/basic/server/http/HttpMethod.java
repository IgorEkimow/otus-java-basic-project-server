package ru.otus.java.basic.server.http;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS;

    public static HttpMethod fromString(String method) {
        if (method == null) return null;
        try {
            return valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}