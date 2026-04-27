package ru.otus.java.basic.server.handler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.http.*;
import ru.otus.java.basic.server.model.Item;
import ru.otus.java.basic.server.repository.ItemRepository;
import ru.otus.java.basic.server.servlet.HttpServlet;
import ru.otus.java.basic.server.util.JsonParser;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ItemHandler extends HttpServlet {
    private final ItemRepository itemRepository;

    public ItemHandler(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        String id = request.getPathVariable("id");

        response.setContentType(MediaType.APPLICATION_JSON);

        if (id != null) {
            Optional<Item> item = itemRepository.findById(Long.parseLong(id));
            if (item.isPresent()) {
                response.setStatus(HttpStatus.OK);
                response.setJsonBody(JsonParser.toJson(item.get()));
            } else {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setJsonBody("{\"error\": \"Item not found\"}");
            }
        } else {
            String queryId = request.getQueryParam("id");
            if (queryId != null) {
                Optional<Item> item = itemRepository.findById(Long.parseLong(queryId));
                if (item.isPresent()) {
                    response.setStatus(HttpStatus.OK);
                    response.setJsonBody(JsonParser.toJson(item.get()));
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND);
                    response.setJsonBody("{\"error\": \"Item not found\"}");
                }
            } else {
                List<Item> items = itemRepository.findAll();
                response.setStatus(HttpStatus.OK);
                response.setJsonBody(JsonParser.toJson(items));
            }
        }
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON);

        String body = request.getBodyAsString();
        if (body == null || body.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Empty request body\"}");
            return;
        }

        try {
            Item item = JsonParser.fromJson(body, Item.class);
            Item saved = itemRepository.save(item);
            response.setStatus(HttpStatus.CREATED);
            response.setJsonBody(JsonParser.toJson(saved));
        } catch (Exception e) {
            log.error("Error creating item", e);
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Invalid request body\"}");
        }
    }

    @Override
    protected void doPut(HttpRequest request, HttpResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON);

        String body = request.getBodyAsString();
        if (body == null || body.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Empty request body\"}");
            return;
        }

        try {
            Item item = JsonParser.fromJson(body, Item.class);
            if (item.getId() == null) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setJsonBody("{\"error\": \"Item id is required\"}");
                return;
            }

            Optional<Item> existing = itemRepository.findById(item.getId());
            if (existing.isEmpty()) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setJsonBody("{\"error\": \"Item not found\"}");
                return;
            }

            Item updated = itemRepository.save(item);
            response.setStatus(HttpStatus.OK);
            response.setJsonBody(JsonParser.toJson(updated));
        } catch (Exception e) {
            log.error("Error updating item", e);
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Invalid request body\"}");
        }
    }

    @Override
    protected void doDelete(HttpRequest request, HttpResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON);

        String id = request.getPathVariable("id");
        if (id == null) {
            id = request.getQueryParam("id");
        }

        if (id == null) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setJsonBody("{\"error\": \"Item id is required\"}");
            return;
        }

        boolean deleted = itemRepository.delete(Long.parseLong(id));
        if (deleted) {
            response.setStatus(HttpStatus.NO_CONTENT);
            response.setBody(new byte[0]);
        } else {
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setJsonBody("{\"error\": \"Item not found\"}");
        }
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, POST, PUT, DELETE, OPTIONS";
    }
}