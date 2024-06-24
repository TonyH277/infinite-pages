-- Delete order items
DELETE FROM order_items;
ALTER TABLE order_items AUTO_INCREMENT = 1;

-- Delete the orders
DELETE FROM orders;
ALTER TABLE orders AUTO_INCREMENT = 1;
