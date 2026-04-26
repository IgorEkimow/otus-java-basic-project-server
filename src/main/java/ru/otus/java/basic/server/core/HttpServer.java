package ru.otus.java.basic.server.core;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.config.ServerConfig;
import ru.otus.java.basic.server.handler.ErrorHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpServer {
    private final ServerConfig config;
    private final ExecutorService threadPool;
    private final ServletContext servletContext;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ErrorHandler errorHandler;

    public HttpServer(ServerConfig config) {
        this.config = config;
        this.threadPool = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.servletContext = new ServletContext();
        this.router = new Router(servletContext);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(config.getPort());
            running = true;
            log.info("Server started on port {}", config.getPort());

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new RequestHandler(clientSocket, router, errorHandler, config));
                } catch (IOException e) {
                    if (running) {
                        log.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error starting server", e);
            throw new RuntimeException("Server startup failed", e);
        } finally {
            shutdown();
        }
    }

    public void stop() {
        running = false;
        shutdown();
    }

    private void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        servletContext.destroy();
        log.info("Server shutdown complete");
    }
}