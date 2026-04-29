package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.servlet.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class StaticFileHandler extends HttpServlet {
    private final String staticPath;

    public StaticFileHandler(String staticPath) {
        this.staticPath = staticPath;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        String fileName = path.startsWith("/") ? path.substring(1) : path;

        if (fileName.contains("..") || fileName.contains(":/") || fileName.contains(":\\")) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Invalid file path\"}");
            return;
        }

        String extension = getFileExtension(fileName);
        MediaType mediaType = MediaType.fromExtension(extension);
        String resourcePath = staticPath + fileName;
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            Path filePath = Paths.get(staticPath, fileName);
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    response.setStatus(HttpStatus.OK);
                    response.setContentType(mediaType);
                    response.setBody(fileContent);
                    return;
                } catch (IOException e) {
                    log.error("Error reading static file from filesystem: {}", fileName, e);
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.setJsonBody("{\"error\": \"Error reading file\"}");
                    return;
                }
            }

            response.setStatus(HttpStatus.NOT_FOUND);
            response.setJsonBody("{\"error\": \"" + fileName + " file not found\"}");

            return;
        }

        try (InputStream inputStream = resourceUrl.openStream()) {
            byte[] fileContent = inputStream.readAllBytes();
            response.setStatus(HttpStatus.OK);
            response.setContentType(mediaType);
            response.setBody(fileContent);
        } catch (IOException e) {
            log.error("Error reading static file: {}", fileName, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setJsonBody("{\"error\": \"Error reading file\"}");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, OPTIONS";
    }
}