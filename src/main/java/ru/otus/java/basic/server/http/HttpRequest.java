package ru.otus.java.basic.server.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private HttpMethod method;
    private String uri;
    private String path;
    private String queryString;
    private Map<String, String> queryParams;
    private Map<String, String> pathVariables;
    private String httpVersion;
    private HttpHeaders headers;
    private byte[] body;

    public HttpRequest() {
        this.headers = new HttpHeaders();
        this.queryParams = new HashMap<>();
        this.pathVariables = new HashMap<>();
    }

    public HttpMethod getMethod() { return method; }
    public void setMethod(HttpMethod method) { this.method = method; }

    public String getUri() { return uri; }
    public void setUri(String uri) {
        this.uri = uri;
        parseUri();
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getQueryString() { return queryString; }

    public Map<String, String> getQueryParams() { return queryParams; }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getPathVariables() { return pathVariables; }

    public void setPathVariable(String name, String value) {
        pathVariables.put(name, value);
    }

    public String getPathVariable(String name) {
        return pathVariables.get(name);
    }

    public String getHttpVersion() { return httpVersion; }
    public void setHttpVersion(String httpVersion) { this.httpVersion = httpVersion; }

    public HttpHeaders getHeaders() { return headers; }
    public void setHeaders(HttpHeaders headers) { this.headers = headers; }

    public byte[] getBody() { return body; }
    public void setBody(byte[] body) { this.body = body; }

    public String getBodyAsString() {
        return body != null ? new String(body, StandardCharsets.UTF_8) : null;
    }

    public MediaType getAcceptType() {
        return headers.getAccept();
    }

    private void parseUri() {
        if (uri == null) return;

        int queryIndex = uri.indexOf('?');
        if (queryIndex >= 0) {
            path = uri.substring(0, queryIndex);
            queryString = uri.substring(queryIndex + 1);
            parseQueryParams();
        } else {
            path = uri;
            queryString = null;
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
    }

    private void parseQueryParams() {
        if (queryString == null || queryString.isEmpty()) return;

        String[] params = queryString.split("&");
        for (String param : params) {
            String[] parts = param.split("=", 2);
            String key = java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            queryParams.put(key, value);
        }
    }
}