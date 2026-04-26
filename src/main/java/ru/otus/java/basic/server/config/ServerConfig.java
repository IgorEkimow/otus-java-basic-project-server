package ru.otus.java.basic.server.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
@Slf4j
public class ServerConfig {
    private int port;
    private int threadPoolSize;
    private long maxRequestSize;
    private long maxResponseSize;
    private String staticFilesPath;
    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private int dbPoolSize;

    private ServerConfig() {}

    public static ServerConfig load() {
        ServerConfig config = new ServerConfig();
        Properties props = new Properties();

        try (InputStream input = ServerConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);

                config.port = Integer.parseInt(props.getProperty("server.port", "8080"));
                config.threadPoolSize = Integer.parseInt(props.getProperty("server.thread-pool-size", "10"));
                config.maxRequestSize = Long.parseLong(props.getProperty("server.max-request-size", "10485760"));
                config.maxResponseSize = Long.parseLong(props.getProperty("server.max-response-size", "10485760"));
                config.staticFilesPath = props.getProperty("server.static-files-path", "static/");
                config.dbHost = props.getProperty("db.host", "localhost");
                config.dbPort = Integer.parseInt(props.getProperty("db.port", "15432"));
                config.dbName = props.getProperty("db.name", "server_db");
                config.dbUsername = props.getProperty("db.username", "server_user");
                config.dbPassword = props.getProperty("db.password", "server_pass");
                config.dbPoolSize = Integer.parseInt(props.getProperty("db.pool.size", "5"));

                log.info("Configuration loaded successfully");
            } else {
                log.warn("application.properties not found, using defaults");
                setDefaults(config);
            }
        } catch (IOException e) {
            log.error("Error loading configuration", e);
            setDefaults(config);
        }

        return config;
    }

    private static void setDefaults(ServerConfig config) {
        config.port = 8080;
        config.threadPoolSize = 10;
        config.maxRequestSize = 10 * 1024 * 1024;
        config.maxResponseSize = 10 * 1024 * 1024;
        config.staticFilesPath = "static/";
        config.dbHost = "localhost";
        config.dbPort = 15432;
        config.dbName = "server_db";
        config.dbUsername = "server_user";
        config.dbPassword = "server_pass";
        config.dbPoolSize = 5;
    }
}