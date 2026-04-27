package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.servlet.HttpServlet;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StaticFileHandler extends HttpServlet {
    private final String staticPath;
    private final Map<String, byte[]> fileCache = new ConcurrentHashMap<>();
    private final Map<String, Long> fileTimestamps = new ConcurrentHashMap<>();

    public StaticFileHandler(String staticPath) {
        this.staticPath = staticPath;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";

            if (!fileExists("/index.html")) {
                serveWelcomePage(response);
                return;
            }
        } else if (path.startsWith("/static/")) {
            path = path.substring(7);
        } else {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        try {
            byte[] content = getFileContent(path);

            String extension = getFileExtension(path);
            MediaType mediaType = MediaType.fromExtension(extension);

            response.setStatus(HttpStatus.OK);
            response.setContentType(mediaType);
            response.setBody(content);
            response.getHeaders().set("Cache-Control", "max-age=3600");
        } catch (IOException e) {
            log.warn("File not found: {}", path);

            if (!path.endsWith("/")) {
                path = path + "/";
            }

            try {
                byte[] content = getFileContent(path + "index.html");
                response.setStatus(HttpStatus.OK);
                response.setContentType(MediaType.TEXT_HTML);
                response.setBody(content);
                response.getHeaders().set("Cache-Control", "max-age=3600");
            } catch (IOException ex) {
                response.setStatus(HttpStatus.NOT_FOUND);
            }
        }
    }

    private boolean fileExists(String path) {
        try {
            Path filePath = getResourcePath(path);
            return filePath != null && Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    private void serveWelcomePage(HttpResponse response) {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        text-align: center;
                        padding: 50px;
                        background-color: #f5f5f5;
                    }
                    h1 { color: #333; }
                    p { color: #666; }
                    a { color: #0066cc; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <h1>Welcome to Java HTTP Server</h1>
                <p>Server is running successfully!</p>
                <p>
                    <a href="/items">View Items</a> |
                    <a href="/static/">Static Files</a>
                </p>
            </body>
            </html>
            """;

        response.setStatus(HttpStatus.OK);
        response.setHtmlBody(html);
    }

    private byte[] getFileContent(String path) throws IOException {
        Path filePath = getResourcePath(path);
        if (filePath != null && Files.exists(filePath)) {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            long lastModified = attrs.lastModifiedTime().toMillis();

            if (fileCache.containsKey(path) && fileTimestamps.getOrDefault(path, 0L) >= lastModified) {
                return fileCache.get(path);
            }

            byte[] content = Files.readAllBytes(filePath);
            fileCache.put(path, content);
            fileTimestamps.put(path, lastModified);
            return content;
        }

        throw new IOException("File not found: " + path);
    }

    private Path getResourcePath(String path) {
        try {
            String resourcePath = path.startsWith("/") ? path.substring(1) : path;
            URL resource = getClass().getClassLoader().getResource(staticPath + resourcePath);
            if (resource != null) {
                return Path.of(resource.toURI());
            }
        } catch (URISyntaxException e) {
            log.error("Error loading resource", e);
        }
        return null;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, HEAD, OPTIONS";
    }
}