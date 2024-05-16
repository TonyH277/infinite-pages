-- Insert admin and user roles into the roles table using Hibernate
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

-- Insert two users into the users table using Hibernate
INSERT INTO users (email, password, first_name, last_name, shipping_address, is_deleted)
VALUES
    ('admin@example.com', '$2a$10$ZQ5K3QN4E5rklgxJ6vWplO3PFKF6vDeH.9y9bCpQEyMjO3yivCAIG', 'Admin', 'User', 'Admin Address', false),
    ('user@example.com', '$2a$10$5Lc3zplhvI9j2io6qwAMV.oJPsHQoLqqrpfLv6ThPpzKiiUrLQfq6', 'Regular', 'User', 'User Address', false);

-- Update the users_roles table to assign roles to users using Hibernate
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = CASE
                                      WHEN u.email = 'admin@example.com' THEN 'ROLE_ADMIN'
                                      ELSE 'ROLE_USER'
    END;
SET @userId = (SELECT id FROM users WHERE email = 'user@example.com');

-- Insert the shopping cart associated with the user
INSERT INTO shopping_carts (user_id)
VALUES (@userId);
