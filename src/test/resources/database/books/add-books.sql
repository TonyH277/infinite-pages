INSERT INTO categories (id, name, description, is_deleted)
VALUES (1, 'Default Category', 'A default category for books', false);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (1, 'Book Title 1', 'Author 1', 'ISBN-00001', 10.99, 'Description for Book 1', 'coverImage1.jpg', false),
       (2, 'Book Title 2', 'Author 2', 'ISBN-00002', 11.99, 'Description for Book 2', 'coverImage2.jpg', false),
       (3, 'Book Title 3', 'Author 3', 'ISBN-00003', 12.99, 'Description for Book 3', 'coverImage3.jpg', false),
       (4, 'Book Title 4', 'Author 4', 'ISBN-00004', 13.99, 'Description for Book 4', 'coverImage4.jpg', false),
       (5, 'Book Title 5', 'Author 5', 'ISBN-00005', 14.99, 'Description for Book 5', 'coverImage5.jpg', false),
       (6, 'Book Title 6', 'Author 6', 'ISBN-00006', 15.99, 'Description for Book 6', 'coverImage6.jpg', false),
       (7, 'Book Title 7', 'Author 7', 'ISBN-00007', 16.99, 'Description for Book 7', 'coverImage7.jpg', false),
       (8, 'Book Title 8', 'Author 8', 'ISBN-00008', 17.99, 'Description for Book 8', 'coverImage8.jpg', false),
       (9, 'Book Title 9', 'Author 9', 'ISBN-00009', 18.99, 'Description for Book 9', 'coverImage9.jpg', false),
       (10, 'Book Title 10', 'Author 10', 'ISBN-00010', 19.99, 'Description for Book 10', 'coverImage10.jpg', false),
       (11, 'Book Title 11', 'Author 11', 'ISBN-00011', 20.99, 'Description for Book 11', 'coverImage11.jpg', false),
       (12, 'Book Title 12', 'Author 12', 'ISBN-00012', 21.99, 'Description for Book 12', 'coverImage12.jpg', false),
       (13, 'Book Title 13', 'Author 13', 'ISBN-00013', 22.99, 'Description for Book 13', 'coverImage13.jpg', false),
       (14, 'Book Title 14', 'Author 14', 'ISBN-00014', 23.99, 'Description for Book 14', 'coverImage14.jpg', false),
       (15, 'Book Title 15', 'Author 15', 'ISBN-00015', 24.99, 'Description for Book 15', 'coverImage15.jpg', false),
       (16, 'Book Title 16', 'Author 16', 'ISBN-00016', 25.99, 'Description for Book 16', 'coverImage16.jpg', false),
       (17, 'Book Title 17', 'Author 17', 'ISBN-00017', 26.99, 'Description for Book 17', 'coverImage17.jpg', false),
       (18, 'Book Title 18', 'Author 18', 'ISBN-00018', 27.99, 'Description for Book 18', 'coverImage18.jpg', false),
       (19, 'Book Title 19', 'Author 19', 'ISBN-00019', 28.99, 'Description for Book 19', 'coverImage19.jpg', false),
       (20, 'Book Title 20', 'Author 20', 'ISBN-00020', 29.99, 'Description for Book 20', 'coverImage20.jpg', false);

INSERT INTO books_categories (book_id, category_id)
SELECT id, 1
FROM books
WHERE isbn IN (
               'ISBN-00001', 'ISBN-00002', 'ISBN-00003', 'ISBN-00004', 'ISBN-00005',
               'ISBN-00006', 'ISBN-00007', 'ISBN-00008', 'ISBN-00009', 'ISBN-00010',
               'ISBN-00011', 'ISBN-00012', 'ISBN-00013', 'ISBN-00014', 'ISBN-00015',
               'ISBN-00016', 'ISBN-00017', 'ISBN-00018', 'ISBN-00019', 'ISBN-00020'
    );
