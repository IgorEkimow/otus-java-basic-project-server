package ru.otus.java.basic.server.servlet;

import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.util.JsonParser;
import java.util.Map;

public class RootRedirectServlet extends HttpServlet {
    private final String apiBasePath;

    public RootRedirectServlet(String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.OK);
        response.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> redirectInfo = Map.of(
            "message", "Please use the API",
            "api_base_path", apiBasePath,
            "routes", Map.of(
                "info", apiBasePath + "/",
                "items", apiBasePath + "/items"
            )
        );

        response.setJsonBody(JsonParser.toJson(redirectInfo));
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, OPTIONS";
    }
}