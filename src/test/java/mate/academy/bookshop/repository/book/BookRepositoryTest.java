package mate.academy.bookshop.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Category;
import mate.academy.bookshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int CUSTOM_PAGE_SIZE = 2;
    private static final long INVALID_CATEGORY_ID = 2L;
    private static final String CATEGORY_NAME_1 = "Category1";
    private static final String CATEGORY_NAME_2 = "Category2";
    private static final String BOOK_TITLE_1 = "Title1";
    private static final String BOOK_TITLE_2 = "Title2";
    private static final String BOOK_TITLE_3 = "Title3";
    private static final String BOOK_ISBN_1 = "234567";
    private static final String BOOK_ISBN_2 = "234560";
    private static final String BOOK_ISBN_3 = "234599";
    private static final String AUTHOR = "Author";
    private static final String DESCRIPTION = "Description";
    private static final String COVER_IMAGE = "Cover image";

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Category category2;
    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName(CATEGORY_NAME_1);
        category2 = new Category();
        category2.setName(CATEGORY_NAME_2);
        category = categoryRepository.save(category);
        category2 = categoryRepository.save(category2);

        book1 = createBook(BOOK_TITLE_1, BOOK_ISBN_1, category);
        book2 = createBook(BOOK_TITLE_2, BOOK_ISBN_2, category);
        book3 = createBook(BOOK_TITLE_3, BOOK_ISBN_3, category);
    }

    @DisplayName("Find all with default pageable params")
    @Test
    void findAll_PageableDefaultParams_ReturnsPageOfBooks() {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        List<Book> books = List.of(book1, book2, book3);
        bookRepository.saveAll(books);
        Page<Book> bookPage = new PageImpl<>(books, pageable, 3);

        Page<Book> response = bookRepository.findAll(pageable);

        assertEquals(DEFAULT_PAGE_SIZE, response.getSize());
        assertEquals(3, response.getTotalElements());
        assertEquals(bookPage, response);
        assertTrue(response.getContent().containsAll(books));
    }

    @DisplayName("Find all with custom pageable params")
    @Test
    void findAll_PageableCustomParams_ReturnsPageOfBooks() {
        Pageable pageable = PageRequest.of(0, CUSTOM_PAGE_SIZE, Sort.by("title").descending());
        List<Book> books = List.of(book1, book2, book3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findAll(pageable);

        assertEquals(CUSTOM_PAGE_SIZE, response.getSize());
        assertEquals(List.of(book3, book2), response.getContent());
    }

    @DisplayName("Find by id when book exists")
    @Test
    void findById_BookExistById_ReturnsOptionalBookWithCategories() {
        Book savedBook = bookRepository.save(book1);

        Optional<Book> response = bookRepository.findById(savedBook.getId());

        assertTrue(response.isPresent());
        Book fetchedBook = response.get();
        assertEquals(1, fetchedBook.getCategories().size());
        assertTrue(fetchedBook.getCategories().contains(category));
    }

    @DisplayName("Find by id when book does not exist")
    @Test
    void findById_BookNotExistById_ReturnsOptionalEmpty() {
        Optional<Book> response = bookRepository.findById(1L);

        assertFalse(response.isPresent());
    }

    @DisplayName("Find by category id with valid category id and default pageable params")
    @Test
    void findByCategoryId_ValidCategoryIdAndDefaultPageableParams_ReturnsPageOfBooks() {
        book2.setCategories(Set.of(category2));
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        List<Book> books = List.of(book1, book2, book3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(category.getId(), pageable);

        assertEquals(DEFAULT_PAGE_SIZE, response.getSize());
        assertEquals(List.of(book1, book3), response.getContent());
        response.forEach(book -> assertTrue(
                book.getCategories().contains(category)));
    }

    @DisplayName("Find by category id with invalid category id and default pageable params")
    @Test
    void findByCategoryId_InvalidCategoryIdAndDefaultPageableParams_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        List<Book> books = List.of(book1, book2, book3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(INVALID_CATEGORY_ID, pageable);

        assertEquals(DEFAULT_PAGE_SIZE, response.getSize());
        assertEquals(Collections.emptyList(), response.getContent());
    }

    @DisplayName("Find by category id with valid category id and custom pageable params")
    @Test
    void findByCategoryId_ValidCategoryIdAndCustomPageableParams_ReturnsCustomPageOfBooks() {
        book2.setCategories(Set.of(category2));
        Pageable pageable = PageRequest.of(0, CUSTOM_PAGE_SIZE);
        List<Book> books = List.of(book1, book2, book3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(category.getId(), pageable);

        assertEquals(CUSTOM_PAGE_SIZE, response.getSize());
        assertEquals(List.of(book1, book3), response.getContent());
        response.forEach(book -> assertTrue(
                book.getCategories().contains(category)));
    }

    private Book createBook(String title, String isbn, Category category) {
        Book book = new Book();
        book.setAuthor(AUTHOR);
        book.setIsbn(isbn);
        book.setPrice(BigDecimal.TEN);
        book.setTitle(title);
        book.setDescription(DESCRIPTION);
        book.setCoverImage(COVER_IMAGE);
        book.setCategories(Set.of(category));
        return book;
    }
}
