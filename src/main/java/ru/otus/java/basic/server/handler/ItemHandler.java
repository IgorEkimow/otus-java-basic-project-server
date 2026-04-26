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

        MediaType acceptType = request.getAcceptType();
        if (!acceptType.equals(MediaType.ALL) && !acceptType.isCompatible(MediaType.APPLICATION_JSON) && !acceptType.isCompatible(MediaType.TEXT_HTML)) {
            response.setStatus(HttpStatus.NOT_ACCEPTABLE);
            response.setJsonBody("{\"error\": \"Not Acceptable\"}");
            return;
        }

        if (id != null) {
            Optional<Item> item = itemRepository.findById(Long.parseLong(id));
            if (item.isPresent()) {
                response.setStatus(HttpStatus.OK);
                if (acceptType.isCompatible(MediaType.TEXT_HTML)) {
                    response.setHtmlBody(generateItemHtml(item.get()));
                } else {
                    response.setJsonBody(JsonParser.toJson(item.get()));
                }
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
                if (acceptType.isCompatible(MediaType.TEXT_HTML)) {
                    response.setHtmlBody(generateItemsHtml(items));
                } else {
                    response.setJsonBody(JsonParser.toJson(items));
                }
            }
        }
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
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
            response.setJsonBody("{\"message\": \"Item deleted\"}");
        } else {
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setJsonBody("{\"error\": \"Item not found\"}");
        }
    }

    @Override
    protected String getSupportedMethods() {
        return "GET, POST, PUT, DELETE, OPTIONS";
    }

    private String generateItemsHtml(List<Item> items) {
        StringBuilder html = new StringBuilder("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Items List</title>
                <link rel="stylesheet" href="/style.css">
            </head>
            <body>
                <h1>Items List</h1>
                <table>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Price</th>
                        <th>Category</th>
                        <th>Quantity</th>
                    </tr>
            """);

        for (Item item : items) {
            html.append("<tr>")
                .append("<td>").append(item.getId()).append("</td>")
                .append("<td>").append(item.getName()).append("</td>")
                .append("<td>").append(item.getDescription()).append("</td>")
                .append("<td>").append(item.getPrice()).append("</td>")
                .append("<td>").append(item.getCategory()).append("</td>")
                .append("<td>").append(item.getQuantity()).append("</td>")
            .append("</tr>");
        }

        html.append("""
                </table>
                <a href="/">Back to Home</a>
            </body>
            </html>
            """);

        return html.toString();
    }

    private String generateItemHtml(Item item) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Item Details</title>
                <link rel="stylesheet" href="/style.css">
            </head>
            <body>
                <h1>Item Details</h1>
                <div>
                    <p><strong>ID:</strong> %d</p>
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Description:</strong> %s</p>
                    <p><strong>Price:</strong> %s</p>
                    <p><strong>Category:</strong> %s</p>
                    <p><strong>Quantity:</strong> %d</p>
                    <p><strong>Created:</strong> %s</p>
                    <p><strong>Updated:</strong> %s</p>
                </div>
                <a href="/items">Back to List</a>
            </body>
            </html>
            """,
                item.getId(),
                item.getName(),
                item.getDescription() != null ? item.getDescription() : "",
                item.getPrice(),
                item.getCategory() != null ? item.getCategory() : "",
                item.getQuantity() != null ? item.getQuantity() : 0,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}