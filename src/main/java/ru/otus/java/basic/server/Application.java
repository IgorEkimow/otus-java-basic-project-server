package ru.otus.java.basic.server;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.config.ServerConfig;
import ru.otus.java.basic.server.core.HttpServer;
import ru.otus.java.basic.server.core.ServletContext;
import ru.otus.java.basic.server.handler.ErrorHandler;
import ru.otus.java.basic.server.handler.ItemHandler;
import ru.otus.java.basic.server.handler.StaticFileHandler;
import ru.otus.java.basic.server.repository.ItemRepository;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
            ItemHandler itemHandler = new ItemHandler(itemRepository);
            StaticFileHandler staticFileHandler = new StaticFileHandler(config.getStaticFilesPath());
            ErrorHandler errorHandler = new ErrorHandler(config.getStaticFilesPath());

            HttpServer server = new HttpServer(config);
            ServletContext servletContext = server.getServletContext();
            servletContext.addServlet("itemServlet", itemHandler);
            servletContext.addMapping("itemServlet", "/items/{id}");
            servletContext.addMapping("itemServlet", "/items");
            servletContext.addServlet("staticServlet", staticFileHandler);
            servletContext.addMapping("staticServlet", "/static/*");
            servletContext.addMapping("staticServlet", "/");
            servletContext.addMapping("staticServlet", "/index.html");
            servletContext.addMapping("staticServlet", "/favicon.ico");

            server.setErrorHandler(errorHandler);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down server...");
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