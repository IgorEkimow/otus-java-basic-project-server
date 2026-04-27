package ru.otus.java.basic.server.handler;

import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.servlet.HttpServlet;
import ru.otus.java.basic.server.util.JsonParser;
import java.util.Map;

public class ApiInfoServlet extends HttpServlet {
    private final String apiVersion;
    private final String apiBasePath;

    public ApiInfoServlet(String apiVersion, String apiBasePath) {
        this.apiVersion = apiVersion;
        this.apiBasePath = apiBasePath;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.OK);
        response.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> apiInfo = Map.of(
            "name", "REST API Server",
            "version", "1.0.0",
            "api_version", apiVersion,
            "base_path", apiBasePath,
            "description", "Item Management REST API",
            "endpoints", Map.of(
                "GET " + apiBasePath + "/items", "Get all items",
                "GET " + apiBasePath + "/items/{id}", "Get item by ID",
                "POST " + apiBasePath + "/items", "Create new item (JSON body)",
                "PUT " + apiBasePath + "/items", "Update existing item (JSON body with id)",
                "DELETE " + apiBasePath + "/items/{id}", "Delete item by ID"
            )
        );

        response.setJsonBody(JsonParser.toJson(apiInfo));
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, OPTIONS";
    }
}