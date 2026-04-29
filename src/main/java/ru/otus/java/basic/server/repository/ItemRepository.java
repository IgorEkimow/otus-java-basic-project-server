package ru.otus.java.basic.server.repository;

import lombok.extern.slf4j.Slf4j;
import ru.otus.java.basic.server.model.Item;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ItemRepository {
    private final DataSource dataSource;

    public ItemRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void initializeSchema(DataSource dataSource) {
        String sql = """
            CREATE TABLE IF NOT EXISTS items (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                price DECIMAL(10, 2) NOT NULL,
                category VARCHAR(100),
                quantity INTEGER DEFAULT 0
            );
        """;

        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Failed to initialize database schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public List<Item> findAll() {
        String sql = "SELECT * FROM items ORDER BY id";
        List<Item> items = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            log.error("Error fetching all items", e);
            throw new RuntimeException("Database error", e);
        }

        return items;
    }

    public Optional<Item> findById(Long id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            log.error("Error fetching item by id: {}", id, e);
            throw new RuntimeException("Database error", e);
        }

        return Optional.empty();
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            return insert(item);
        } else {
            return update(item);
        }
    }

    private Item insert(Item item) {
        String sql = """
            INSERT INTO items (name, description, price, category, quantity) 
            VALUES (?, ?, ?, ?, ?)
            RETURNING *;
        """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getCategory());
            stmt.setInt(5, item.getQuantity() != null ? item.getQuantity() : 0);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            log.error("Error inserting item", e);
            throw new RuntimeException("Database error", e);
        }

        return null;
    }

    private Item update(Item item) {
        String sql = """
            UPDATE items 
            SET name = ?, description = ?, price = ?, category = ?, quantity = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *;
        """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getCategory());
            stmt.setInt(5, item.getQuantity() != null ? item.getQuantity() : 0);
            stmt.setLong(6, item.getId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            log.error("Error updating item: {}", item.getId(), e);
            throw new RuntimeException("Database error", e);
        }

        return null;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            log.error("Error deleting item: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        return Item.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .price(rs.getBigDecimal("price"))
            .category(rs.getString("category"))
            .quantity(rs.getInt("quantity"))
            .build();
    }
}