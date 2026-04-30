package ru.otus.java.basic.server.servlet;

import ru.otus.java.basic.server.http.HttpRequest;
import ru.otus.java.basic.server.http.HttpResponse;

@FunctionalInterface
public interface Servlet {
    void service(HttpRequest request, HttpResponse response) throws Exception;

    default void init() {}
    default void destroy() {}
}