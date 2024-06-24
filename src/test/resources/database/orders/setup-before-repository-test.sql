-- Insert book
INSERT INTO books (id, title, author, isbn, price)
VALUES (1, 'Test Book', 'Test Author', '123456789', 10);

-- Insert order
INSERT INTO orders (id, user_id, order_date, status, total, shipping_address)
VALUES (1, 1, '2024-01-01 00:00:00', 'PENDING', 10, 'test');

-- Insert order item
INSERT INTO order_items (id, book_id, order_id, price, quantity)
VALUES (1, 1, 1, 10, 2);
