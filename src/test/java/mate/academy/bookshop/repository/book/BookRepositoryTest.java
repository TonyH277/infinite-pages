package mate.academy.bookshop.repository.book;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Category;
import mate.academy.bookshop.repository.CategoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Book book1;
    private Book book2;
    private Book book3;


    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Category1");
        category = categoryRepository.save(category);

        book1 = createBook("Title1", "234567", category);
        book2 = createBook("Title2", "234560", category);
        book3 = createBook("Title3", "234599", category);
    }

    @Test
    void findAll_PageableDefaultParams_ReturnsPageOfBooks() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(books, pageable, 3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findAll(pageable);

        Assertions.assertEquals(20, response.getSize());
        Assertions.assertEquals(3, response.getTotalElements());
        Assertions.assertEquals(bookPage, response);
        Assertions.assertTrue(response.getContent().containsAll(books));
    }

    @Test
    void findAll_PageableCustomParams_ReturnsPageOfBooks() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("title").descending());
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(List.of(book3, book2), pageable, 3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findAll(pageable);

        Assertions.assertEquals(2, response.getSize());
        Assertions.assertEquals(List.of(book3, book2), response.getContent());
    }

    @Test
    void findById_BookExistById_ReturnsOptionalBookWithCategories() {
        Book savedBook = bookRepository.save(book1);

        Optional<Book> response = bookRepository.findById(savedBook.getId());

        Assertions.assertTrue(response.isPresent());
        Book fetchedBook = response.get();
        Assertions.assertEquals(1, fetchedBook.getCategories().size());
        Assertions.assertTrue(fetchedBook.getCategories().contains(category));
    }

    @Test
    void findById_BookNotExistById_ReturnsOptionalEmpty() {
        Optional<Book> response = bookRepository.findById(1L);

        Assertions.assertFalse(response.isPresent());
        Assertions.assertEquals(Optional.empty(), response);
    }

    @Test
    void findByCategoryId_ValidCategoryIdAndDefaultPageableParams_ReturnsPageOfBooks() {
        Category category2 = new Category();
        category2.setName("Category2");
        category2.setDescription("Description2");
        Category secondCategory = categoryRepository.save(category2);

        book2.setCategories(Set.of(secondCategory));
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book3), pageable, 3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(category.getId(), pageable);

        Assertions.assertEquals(20, response.getSize());
        Assertions.assertEquals(List.of(book1, book3), response.getContent());
        response.forEach(book -> Assertions.assertTrue(
                book.getCategories().contains(category)));
    }

    @Test
    void findByCategoryId_InvalidCategoryIdAndDefaultPageableParams_ReturnsEmptyPage() {
        Long invalidCategoryId = 2L;
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(Collections.emptyList(), pageable, 3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(invalidCategoryId, pageable);

        Assertions.assertEquals(20, response.getSize());
        Assertions.assertEquals(Collections.emptyList(), response.getContent());
    }

    @Test
    void findByCategoryId_ValidCategoryIdAndCustomPageableParams_ReturnsCustomPageOfBooks() {
        Category category2 = new Category();
        category2.setName("Category2");
        category2.setDescription("Description2");
        Category secondCategory = categoryRepository.save(category2);
        book2.setCategories(Set.of(secondCategory));

        Pageable pageable = PageRequest.of(0, 2);
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book3), pageable, 3);
        bookRepository.saveAll(books);

        Page<Book> response = bookRepository.findByCategoryId(category.getId(), pageable);

        Assertions.assertEquals(2, response.getSize());
        Assertions.assertEquals(List.of(book1, book3), response.getContent());
        response.forEach(book -> Assertions.assertTrue(
                book.getCategories().contains(category)));

    }

    private Book createBook(String title, String isbn, Category category) {
        Book book = new Book();
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setPrice(BigDecimal.TEN);
        book.setTitle(title);
        book.setDescription("Description");
        book.setCoverImage("coverImage");
        book.setCategories(Set.of(category));
        return book;
    }
}
