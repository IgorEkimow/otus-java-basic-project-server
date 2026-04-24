package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.HttpStatus;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ErrorHandler {
    private final String staticPath;

    public ErrorHandler(String staticPath) {
        this.staticPath = staticPath;
    }

    public byte[] handleError(HttpStatus status) {
        String errorPage = switch (status.code()) {
            case 400 -> "400.html";
            case 404 -> "404.html";
            case 405 -> "405.html";
            case 406 -> "406.html";
            case 500 -> "500.html";
            default -> "error.html";
        };

        try {
            return loadErrorPage(errorPage);
        } catch (IOException e) {
            log.warn("Could not load error page: {}", errorPage, e);
            return generateDefaultErrorPage(status);
        }
    }

    private byte[] loadErrorPage(String page) throws IOException {
        Path path = getResourcePath(page);
        if (path != null) {
            return Files.readAllBytes(path);
        }
        throw new IOException("Error page not found: " + page);
    }

    private Path getResourcePath(String path) {
        try {
            URL resource = getClass().getClassLoader().getResource(staticPath + path);
            if (resource != null) {
                return Path.of(resource.toURI());
            }
        } catch (URISyntaxException e) {
            log.error("Error loading resource", e);
        }
        return null;
    }

    private byte[] generateDefaultErrorPage(HttpStatus status) {
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%d %s</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px; 
                        background-color: #f5f5f5;
                    }
                    h1 { font-size: 72px; margin: 0; color: #e74c3c; }
                    p { font-size: 24px; color: #333; }
                </style>
            </head>
            <body>
                <h1>%d</h1>
                <p>%s</p>
            </body>
            </html>
            """, status.code(), status.reason(), status.code(), status.reason());

        return html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}