package ru.otus.java.basic.server.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private HttpStatus status;
    private HttpHeaders headers;
    private byte[] body;
    private Map<String, Object> attributes;

    public HttpResponse() {
        this.status = HttpStatus.OK;
        this.headers = new HttpHeaders();
        this.attributes = new HashMap<>();
    }

    public HttpStatus getStatus() { return status; }
    public void setStatus(HttpStatus status) { this.status = status; }

    public HttpHeaders getHeaders() { return headers; }
    public void setHeaders(HttpHeaders headers) { this.headers = headers; }

    public byte[] getBody() { return body; }
    public void setBody(byte[] body) { this.body = body; }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    public void setJsonBody(String json) {
        headers.set("Content-Type", "application/json");
        setBody(json);
    }

    public void setHtmlBody(String html) {
        headers.set("Content-Type", "text/html; charset=UTF-8");
        setBody(html);
    }

    public void setContentType(MediaType mediaType) {
        headers.set("Content-Type", mediaType.toString() + "; charset=UTF-8");
    }

    public void setContentLength() {
        if (body != null) {
            headers.set("Content-Length", String.valueOf(body.length));
        } else {
            headers.set("Content-Length", "0");
        }
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public byte[] toBytes() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("HTTP/1.1 ")
            .append(status.code())
            .append(" ")
            .append(status.reason())
            .append("\r\n");

        setContentLength();
        headerBuilder.append(headers.toString());
        headerBuilder.append("\r\n");

        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);

        if (body != null && body.length > 0) {
            byte[] response = new byte[headerBytes.length + body.length];
            System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
            System.arraycopy(body, 0, response, headerBytes.length, body.length);
            return response;
        }

        return headerBytes;
    }
}