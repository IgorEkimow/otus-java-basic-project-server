package ru.otus.java.basic.server.http;

import java.util.*;

public class HttpHeaders {
    private final Map<String, List<String>> headers;

    public HttpHeaders() {
        this.headers = new LinkedHashMap<>();
    }

    public void add(String name, String value) {
        headers.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>()).add(value);
    }

    public void set(String name, String value) {
        headers.put(name.toLowerCase(), new ArrayList<>(List.of(value)));
    }

    public String get(String name) {
        List<String> values = headers.get(name.toLowerCase());

        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    public List<String> getAll(String name) {
        return headers.getOrDefault(name.toLowerCase(), List.of());
    }

    public boolean contains(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public MediaType getAccept() {
        String accept = get("accept");

        return MediaType.parse(accept);
    }

    public String getContentType() {
        return get("content-type");
    }

    public int getContentLength() {
        String length = get("content-length");

        return length != null ? Integer.parseInt(length) : 0;
    }

    public Map<String, List<String>> getAllHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        headers.forEach((key, values) -> {
            values.forEach(value -> sb.append(key).append(": ").append(value).append("\r\n"));
        });

        return sb.toString();
    }
}