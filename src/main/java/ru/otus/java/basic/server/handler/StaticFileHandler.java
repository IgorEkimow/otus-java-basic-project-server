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

        if (path.startsWith("/static/")) {
            path = path.substring(8);
        } else if (path.equals("/static")) {
            path = "/index.html";
        }

        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
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
            response.setStatus(HttpStatus.NOT_FOUND);
        }
    }

    private byte[] getFileContent(String path) throws IOException {
        Path filePath = getResourcePath(path);
        if (filePath != null) {
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
            URL resource = getClass().getClassLoader().getResource(staticPath + path);
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
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, HEAD, OPTIONS";
    }
}