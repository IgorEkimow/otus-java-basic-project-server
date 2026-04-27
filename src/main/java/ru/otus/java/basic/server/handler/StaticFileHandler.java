package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.servlet.HttpServlet;

@Slf4j
public class StaticFileHandler extends HttpServlet {
    public StaticFileHandler(String staticPath) {
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND);
        response.setContentType(MediaType.APPLICATION_JSON);
        response.setJsonBody("{\"error\": \"Static file serving is disabled. This is a REST API only.\"}");
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, OPTIONS";
    }
}