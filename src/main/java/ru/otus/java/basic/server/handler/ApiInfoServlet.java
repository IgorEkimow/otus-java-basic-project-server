package ru.otus.java.basic.server.handler;

import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.servlet.HttpServlet;
import ru.otus.java.basic.server.util.JsonParser;
import java.util.Map;

public class ApiInfoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.OK);
        response.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> apiInfo = Map.of(
            "name", "REST API Server",
            "version", "1.0.0",
            "description", "Item Management REST API",
            "endpoints", Map.of(
                "GET /items", "Get all items",
                "GET /items/{id}", "Get item by ID",
                "POST /items", "Create new item (JSON body)",
                "PUT /items", "Update existing item (JSON body with id)",
                "DELETE /items/{id}", "Delete item by ID"
            )
        );

        response.setJsonBody(JsonParser.toJson(apiInfo));
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, OPTIONS";
    }
}