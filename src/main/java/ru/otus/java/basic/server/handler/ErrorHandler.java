package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.HttpStatus;
import ru.otus.java.basic.server.util.JsonParser;
import java.util.Map;

@Slf4j
public class ErrorHandler {
    private final String apiBasePath;

    public ErrorHandler(String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    public byte[] handleError(HttpStatus status) {
        Map<String, Object> errorResponse = Map.of(
            "status", status.code(),
            "error", status.reason(),
            "message", getErrorMessage(status),
            "api_base_path", apiBasePath
        );

        String json = JsonParser.toJson(errorResponse);

        return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String getErrorMessage(HttpStatus status) {
        return switch (status.code()) {
            case 400 -> "Bad Request - Invalid request format or parameters";
            case 404 -> "Not Found - The requested resource does not exist";
            case 405 -> "Method Not Allowed - HTTP method is not supported for this endpoint";
            case 406 -> "Not Acceptable - Requested format is not supported";
            case 500 -> "Internal Server Error - An unexpected error occurred";
            default -> "An error occurred while processing your request";
        };
    }
}