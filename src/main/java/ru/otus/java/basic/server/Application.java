package ru.otus.java.basic.server;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.config.ServerConfig;
import ru.otus.java.basic.server.core.HttpServer;
import ru.otus.java.basic.server.core.ServletContext;
import ru.otus.java.basic.server.handler.ApiInfoServlet;
import ru.otus.java.basic.server.handler.ErrorHandler;
import ru.otus.java.basic.server.handler.ItemHandler;
import ru.otus.java.basic.server.handler.StaticFileHandler;
import ru.otus.java.basic.server.repository.ItemRepository;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.otus.java.basic.server.servlet.RootRedirectServlet;

@Slf4j
public class Application {
    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.load();

            if (args.length > 0) {
                config.setPort(Integer.parseInt(args[0]));
            }

            DataSource dataSource = createDataSource(config);
            ItemRepository.initializeSchema(dataSource);
            ItemRepository itemRepository = new ItemRepository(dataSource);
            ErrorHandler errorHandler = new ErrorHandler(config.getApiBasePath());

            HttpServer server = new HttpServer(config);
            ServletContext servletContext = server.getServletContext();
            String basePath = config.getApiBasePath();

            servletContext.addServlet("itemServlet", new ItemHandler(itemRepository, config.getApiBasePath()));
            servletContext.addMapping("itemServlet", basePath + "/items/{id}");
            servletContext.addMapping("itemServlet", basePath + "/items");

            servletContext.addServlet("apiInfoServlet", new ApiInfoServlet(config.getApiVersion(), basePath));
            servletContext.addMapping("apiInfoServlet", basePath + "/");
            servletContext.addMapping("apiInfoServlet", basePath);

            servletContext.addServlet("rootRedirect", new RootRedirectServlet(basePath));
            servletContext.addMapping("rootRedirect", "/");

            servletContext.addServlet("staticFileHandler", new StaticFileHandler(config.getStaticFilesPath()));
            servletContext.addMapping("staticFileHandler", "/*");

            server.setErrorHandler(errorHandler);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
                if (dataSource instanceof HikariDataSource hds) {
                    hds.close();
                }
            }));

            server.start();
        } catch (Exception e) {
            log.error("Failed to start application", e);
            System.exit(1);
        }
    }

    private static DataSource createDataSource(ServerConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", config.getDbHost(), config.getDbPort(), config.getDbName()));
        hikariConfig.setUsername(config.getDbUsername());
        hikariConfig.setPassword(config.getDbPassword());
        hikariConfig.setMaximumPoolSize(config.getDbPoolSize());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setMaxLifetime(1800000);

        return new HikariDataSource(hikariConfig);
    }
}