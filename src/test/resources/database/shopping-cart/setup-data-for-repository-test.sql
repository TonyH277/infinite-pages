-- Insert book
INSERT INTO books (id, title, author, isbn, price)
VALUES (1, 'Test Book', 'Test Author', '123456789', 10);

-- Insert cart item
INSERT INTO cart_items (id, book_id, shopping_cart_id, quantity)
VALUES (1, 1, 1, 10);
