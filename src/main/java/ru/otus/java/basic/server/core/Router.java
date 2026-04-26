package ru.otus.java.basic.server.core;

import ru.otus.java.basic.server.http.*;
import java.util.*;

public class Router {
    private final ServletContext servletContext;

    public Router(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void route(HttpRequest request, HttpResponse response) throws Exception {
        String path = request.getPath();

        Optional<ServletContext.ServletMatch> match = servletContext.findServlet(path);

        if (match.isPresent()) {
            ServletContext.ServletMatch servletMatch = match.get();
            Map<String, String> pathVars = servletMatch.pathVariables();
            if (pathVars != null) {
                pathVars.forEach((key, value) -> {
                    request.setPathVariable(key, value);
                });
            }

            if (!isMethodSupported(servletMatch, request.getMethod())) {
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                return;
            }

            if (request.getMethod() == HttpMethod.GET && !isAcceptable(request)) {
                response.setStatus(HttpStatus.NOT_ACCEPTABLE);
                return;
            }

            servletMatch.servlet().service(request, response);
        } else {
            response.setStatus(HttpStatus.NOT_FOUND);
        }
    }

    private boolean isMethodSupported(ServletContext.ServletMatch match, HttpMethod method) {
        return true;
    }

    private boolean isAcceptable(HttpRequest request) {
        MediaType acceptType = request.getAcceptType();
        return acceptType != null;
    }
}