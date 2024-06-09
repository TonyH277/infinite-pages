-- Insert two orders for the user with id 1
INSERT INTO orders (user_id, status, total, order_date, shipping_address)
VALUES
    (2, 'PENDING', 57.95, '2024-06-09 10:00:00', 'Test address'),
    (2, 'COMPLETED', 68.95, '2024-06-09 11:00:00', 'Test address');

-- Insert order items for the first order
INSERT INTO order_items (order_id, book_id, quantity, price)
VALUES
    (1, 1, 2, 10.99),
    (1, 2, 3, 11.99);

-- Insert order items for the second order
INSERT INTO order_items (order_id, book_id, quantity, price)
VALUES
    (2, 3, 1, 12.99),
    (2, 4, 4, 13.99);
