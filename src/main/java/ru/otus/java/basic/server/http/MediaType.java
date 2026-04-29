package ru.otus.java.basic.server.http;

import java.util.HashMap;
import java.util.Map;

public record MediaType(String type, String subtype) {
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json");
    public static final MediaType TEXT_HTML = new MediaType("text", "html");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");
    public static final MediaType TEXT_CSS = new MediaType("text", "css");
    public static final MediaType APPLICATION_JAVASCRIPT = new MediaType("application", "javascript");
    public static final MediaType IMAGE_PNG = new MediaType("image", "png");
    public static final MediaType IMAGE_JPEG = new MediaType("image", "jpeg");
    public static final MediaType ALL = new MediaType("*", "*");

    private static final Map<String, MediaType> EXTENSION_MAP = new HashMap<>();

    static {
        EXTENSION_MAP.put("html", TEXT_HTML);
        EXTENSION_MAP.put("htm", TEXT_HTML);
        EXTENSION_MAP.put("css", TEXT_CSS);
        EXTENSION_MAP.put("js", APPLICATION_JAVASCRIPT);
        EXTENSION_MAP.put("json", APPLICATION_JSON);
        EXTENSION_MAP.put("png", IMAGE_PNG);
        EXTENSION_MAP.put("jpg", IMAGE_JPEG);
        EXTENSION_MAP.put("jpeg", IMAGE_JPEG);
        EXTENSION_MAP.put("txt", TEXT_PLAIN);
    }

    public static MediaType fromExtension(String extension) {
        if (extension == null) return TEXT_PLAIN;

        return EXTENSION_MAP.getOrDefault(extension.toLowerCase(), TEXT_PLAIN);
    }

    public static MediaType parse(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isEmpty() || acceptHeader.equals("*/*")) {
            return ALL;
        }

        String[] parts = acceptHeader.split(",")[0].trim().split("/");
        if (parts.length == 2) {
            return new MediaType(parts[0].trim(), parts[1].trim().split(";")[0].trim());
        }

        return ALL;
    }

    public boolean isCompatible(MediaType other) {
        if (this.equals(ALL) || other.equals(ALL)) return true;
        if (this.type.equals("*") || other.type.equals("*")) return true;

        return this.type.equals(other.type) && (this.subtype.equals("*") || other.subtype.equals("*") || this.subtype.equals(other.subtype));
    }

    @Override
    public String toString() {
        return type + "/" + subtype;
    }
}