package ru.otus.java.basic.server.servlet;

import ru.otus.java.basic.server.http.*;

public abstract class HttpServlet implements Servlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        switch (request.getMethod()) {
            case GET -> doGet(request, response);
            case POST -> doPost(request, response);
            case PUT -> doPut(request, response);
            case DELETE -> doDelete(request, response);
            case HEAD -> doHead(request, response);
            case OPTIONS -> doOptions(request, response);
            default -> {
                response.setStatus(HttpStatus.NOT_IMPLEMENTED);
                response.setBody("Method not implemented");
            }
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected void doPut(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected void doDelete(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected void doHead(HttpRequest request, HttpResponse response) throws Exception {
        doGet(request, response);
        response.setBody(new byte[0]);
    }

    protected void doOptions(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(HttpStatus.OK);
        response.getHeaders().set("Allow", getSupportedMethods());
    }

    protected abstract String getSupportedMethods();
}