CREATE SEQUENCE IF NOT EXISTS items_id_index;

CREATE TABLE IF NOT EXISTS items (
    id BIGINT DEFAULT nextval('items_id_index') PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_items_category ON items(category);
CREATE INDEX IF NOT EXISTS idx_items_name ON items(name);

INSERT INTO items (name, description, price, category, quantity) VALUES
('Apple', 'Juicy green apples', 30.99, 'Fruit', 10),
('Potato', 'Belarusian potatoes', 20.50, 'Vegetables', 30),
('Lemon', 'Very sour', 49.99, 'Fruit', 3),
('Milk', 'Village milk', 120, 'Dairy', 2),
('Cheese', 'Long-term aging', 400, 'Dairy', 1);