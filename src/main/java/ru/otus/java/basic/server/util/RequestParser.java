package ru.otus.java.basic.server.util;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.HttpHeaders;
import ru.otus.java.basic.server.http.HttpMethod;
import ru.otus.java.basic.server.http.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestParser {
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_HEADER_SIZE = 65536;

    public static HttpRequest parse(InputStream inputStream, long maxRequestSize) throws IOException {
        HttpRequest request = new HttpRequest();
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int totalRead = 0;
        int headerEndIndex = -1;

        while (totalRead < MAX_HEADER_SIZE) {
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) break;

            headerBuffer.write(buffer, 0, bytesRead);
            totalRead += bytesRead;

            byte[] data = headerBuffer.toByteArray();
            headerEndIndex = findHeaderEnd(data);
            if (headerEndIndex >= 0) break;
        }

        if (headerEndIndex < 0) {
            throw new IOException("Headers too large or incomplete");
        }

        byte[] headerData = headerBuffer.toByteArray();
        String headerString = new String(headerData, 0, headerEndIndex, StandardCharsets.UTF_8);
        parseHeaders(request, headerString);

        int contentLength = request.getHeaders().getContentLength();
        if (contentLength > 0) {
            if (contentLength > maxRequestSize) {
                throw new IOException("Request body exceeds maximum size");
            }

            int bodyStartIndex = headerEndIndex + 4;
            byte[] bodyData = new byte[contentLength];
            int alreadyRead = headerData.length - bodyStartIndex;

            if (alreadyRead > 0) {
                System.arraycopy(headerData, bodyStartIndex, bodyData, 0, Math.min(alreadyRead, contentLength));
            }

            int remainingBytes = contentLength - alreadyRead;
            if (remainingBytes > 0) {
                int bytesRead = inputStream.read(bodyData, alreadyRead, remainingBytes);
                if (bytesRead != remainingBytes) {
                    log.warn("Expected {} bytes but read {} bytes", remainingBytes, bytesRead);
                }
            }

            request.setBody(bodyData);
        }

        return request;
    }

    private static int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i+1] == '\n' && data[i+2] == '\r' && data[i+3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static void parseHeaders(HttpRequest request, String headerString) {
        String[] lines = headerString.split("\r\n");

        if (lines.length > 0) {
            String[] requestLine = lines[0].split(" ");
            if (requestLine.length >= 3) {
                request.setMethod(HttpMethod.fromString(requestLine[0]));
                request.setUri(requestLine[1]);
                request.setHttpVersion(requestLine[2]);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.add(name, value);
            }
        }
        request.setHeaders(headers);
    }
}