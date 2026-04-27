package ru.otus.java.basic.server.core;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.config.ServerConfig;
import ru.otus.java.basic.server.exception.*;
import ru.otus.java.basic.server.handler.ErrorHandler;
import ru.otus.java.basic.server.http.*;
import java.io.*;
import java.net.Socket;

@Slf4j
public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final ErrorHandler errorHandler;
    private final ServerConfig config;

    public RequestHandler(Socket clientSocket, Router router, ErrorHandler errorHandler, ServerConfig config) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.errorHandler = errorHandler;
        this.config = config;
    }

    @Override
    public void run() {
        try (clientSocket;
             InputStream input = new BufferedInputStream(clientSocket.getInputStream());
             OutputStream output = new BufferedOutputStream(clientSocket.getOutputStream())) {

            clientSocket.setSoTimeout(30000);

            HttpRequest request = ru.otus.java.basic.server.util.RequestParser.parse(input, config.getMaxRequestSize());
            HttpResponse response = new HttpResponse();

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatus(HttpStatus.OK);
                response.getHeaders().set("Access-Control-Allow-Origin", "*");
                response.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.getHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                response.getHeaders().set("Access-Control-Max-Age", "86400");

                byte[] responseBytes = response.toBytes();
                output.write(responseBytes);
                output.flush();
                return;
            }

            try {
                router.route(request, response);
            } catch (NotFoundException e) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.NOT_FOUND)));
            } catch (BadRequestException e) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.BAD_REQUEST)));
            } catch (MethodNotAllowedException e) {
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.METHOD_NOT_ALLOWED)));
            } catch (Exception e) {
                log.error("Error processing request", e);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.INTERNAL_SERVER_ERROR)));
            }

            if (response.getStatus() == HttpStatus.NOT_FOUND && response.getBody() == null) {
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.NOT_FOUND)));
            } else if (response.getStatus() == HttpStatus.METHOD_NOT_ALLOWED && response.getBody() == null) {
                response.setHtmlBody(new String(errorHandler.handleError(HttpStatus.METHOD_NOT_ALLOWED)));
            }

            response.getHeaders().set("Access-Control-Allow-Origin", "*");
            response.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            byte[] responseBytes = response.toBytes();
            if (responseBytes.length > config.getMaxResponseSize()) {
                log.warn("Response size exceeds maximum: {}", responseBytes.length);
                HttpResponse errorResponse = new HttpResponse();

                errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                errorResponse.setBody("Response too large");
                errorResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
                errorResponse.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                errorResponse.getHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                responseBytes = errorResponse.toBytes();
            }

            output.write(responseBytes);
            output.flush();
        } catch (IOException e) {
            log.error("Error handling request", e);
        }
    }
}