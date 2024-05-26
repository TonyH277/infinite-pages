package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookshop.dto.book.BookDto;
import mate.academy.bookshop.dto.book.BookSearchParametersDto;
import mate.academy.bookshop.dto.book.CreateBookRequestDto;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.mapper.BookMapper;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Category;
import mate.academy.bookshop.repository.CategoryRepository;
import mate.academy.bookshop.repository.book.BookRepository;
import mate.academy.bookshop.repository.book.BookSpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookSpecificationBuilder specificationBuilder;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private CreateBookRequestDto requestDto;

    private Book book;

    @BeforeEach
    void setUp() {
        requestDto = new CreateBookRequestDto();
        requestDto.setAuthor("Author");
        requestDto.setIsbn("2131231");
        requestDto.setPrice(BigDecimal.TEN);
        requestDto.setTitle("Title");
        requestDto.setDescription("Description");
        requestDto.setCoverImage("coverImage");
        requestDto.setCategories(Set.of(1L));

        book = new Book();
        book.setAuthor(requestDto.getAuthor());
        book.setIsbn(requestDto.getIsbn());
        book.setPrice(requestDto.getPrice());
        book.setTitle(requestDto.getTitle());
        book.setDescription(requestDto.getDescription());
        book.setCoverImage(requestDto.getCoverImage());
        book.setCategories(Set.of(new Category()));
    }

    @DisplayName("Save book")
    @Test
    public void save_ValidBook_returnsBookDto() {
        BookDto expected = new BookDto();
        expected.setAuthor(requestDto.getAuthor());
        expected.setPrice(requestDto.getPrice());
        expected.setTitle(requestDto.getTitle());
        expected.setDescription(requestDto.getDescription());
        expected.setCoverImage(requestDto.getCoverImage());
        expected.setCategoryIds(Set.of(1L));

        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(new Category()));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto response = bookService.save(requestDto);

        assertEquals(expected, response);
    }

    @DisplayName("Save book without category - throws EntityNotFoundException")
    @Test
    public void save_BookWithoutCategory_ThrowsEntityNotFoundException() {
        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> bookService.save(requestDto));

        String expected = "There is no category with id " + 1;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @DisplayName("Find all book default pageable params")
    @Test
    public void findAll_DefaultPageableParams_ReturnsAllBookDtos() {
        Book book2 = createBook("Title2", "12345");
        Page<Book> bookPage = new PageImpl<>(List.of(book, book2));
        Pageable defaultPageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(defaultPageable)).thenReturn(bookPage);
        BookDto bookDto = bookToDto(book);
        BookDto book2Dto = bookToDto(book2);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookDto, book2Dto);

        List<BookDto> response = bookService.findAll(defaultPageable);

        assertEquals(2, response.size());
        assertEquals(bookDto, response.get(0));
        assertEquals(book2Dto, response.get(1));
    }

    @DisplayName("Find all books custom pageable params")
    @Test
    public void findAll_Page1Size1_ReturnsBookWithId3() {
        Book book2 = createBook("Title2", "12345");
        Pageable pageable = PageRequest.of(1, 1, Sort.by("title").ascending());
        Page<Book> bookPage = new PageImpl<>(List.of(book2), pageable, 3);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        BookDto book2Dto = bookToDto(book2);
        when(bookMapper.toDto(any(Book.class))).thenReturn(book2Dto);

        List<BookDto> response = bookService.findAll(pageable);

        assertEquals(1, response.size());
        assertEquals(book2Dto, response.get(0));
    }

    @DisplayName("Find all books sorted in reverse order")
    @Test
    public void findAll_SortedReverseOrder_ReturnsAllBookDtosReverseOrder() {
        Book book2 = createBook("Title2", "12345");
        Book book3 = createBook("Title3", "123456");
        Pageable pageable = PageRequest.of(0, 20, Sort.by("title").descending());
        Page<Book> pageBooks = new PageImpl<>(List.of(book3, book2, book));
        when(bookRepository.findAll(pageable)).thenReturn(pageBooks);
        BookDto bookDto = bookToDto(book);
        BookDto book2Dto = bookToDto(book2);
        BookDto book3Dto = bookToDto(book3);
        when(bookMapper.toDto(any(Book.class))).thenReturn(book3Dto, book2Dto, bookDto);

        List<BookDto> response = bookService.findAll(pageable);

        assertEquals(3, response.size());
        assertEquals(book3Dto, response.get(0));
        assertEquals(book2Dto, response.get(1));
        assertEquals(bookDto, response.get(2));
    }

    @DisplayName("Find all books when no books present")
    @Test
    public void findAll_NoBooksPresent_ReturnsEmptyList() {
        when(bookRepository.findAll(Pageable.unpaged())).thenReturn(Page.empty());

        List<BookDto> response = bookService.findAll(Pageable.unpaged());

        assertEquals(List.of(), response);
    }

    @DisplayName("Find book by id with valid id")
    @Test
    public void findBookById_ValidId_ReturnsBookDto() {
        BookDto bookDto = bookToDto(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookDto response = bookService.findBookById(1L);

        assertEquals(bookDto, response);
    }

    @DisplayName("Find book by id with invalid id")
    @Test
    public void findBookById_InvalidId_ThrowsEntityNotFoundException() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> bookService.findBookById(id));

        String actual = exception.getMessage();
        String expected = "Can't find book with id " + id;

        assertEquals(expected, actual);
    }

    @DisplayName("Update book with valid data")
    @Test
    public void update_Book_Success() {
        Long id = 1L;
        CreateBookRequestDto requestDto = getCreateBookRequestDto();

        Book updatedBook = updateBook(book, requestDto);
        BookDto bookDto = bookToDto(updatedBook);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(updatedBook)).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(bookDto);

        BookDto response = bookService.update(id, requestDto);

        assertEquals(bookDto, response);
    }

    @DisplayName("Update book with invalid id")
    @Test
    public void update_InvalidBookId_ThrowsEntityNotFoundException() {
        Long id = 1L;
        CreateBookRequestDto requestDto = getCreateBookRequestDto();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> bookService.update(id, requestDto));

        String actual = exception.getMessage();
        String expected = "Can't find book with id " + id;

        assertEquals(expected, actual);
    }

    @DisplayName("Delete book by id with valid id")
    @Test
    public void deleteById_ValidBookId_DeleteBookFromDatabase() {
        Long id = 1L;
        when(bookRepository.existsById(id)).thenReturn(true);

        bookService.deleteById(id);

        verify(bookRepository).deleteById(id);
    }

    @DisplayName("Delete book by id with invalid id")
    @Test
    public void deleteById_InvalidBookId_ThrowsEntityNotFoundException() {
        Long id = 1L;
        when(bookRepository.existsById(id)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> bookService.deleteById(id));

        String actual = exception.getMessage();
        String expected = "Can't find book with id " + id;

        assertEquals(expected, actual);
    }

    @DisplayName("Search books with default pageable and valid params")
    @Test
    public void search_DefaultPageableAndDefaultParams_ReturnsZeroPage20SizeBooksDtos() {
        BookSearchParametersDto params = new BookSearchParametersDto();
        params.setAuthors(new String[]{"Author1", "Author2"});
        params.setTitles(new String[]{"Title1", "Title2"});
        params.setPriceRange(new String[]{"10", "40"});

        Pageable pageable = PageRequest.of(0, 20);

        Specification<Book> bookSpecification = (root, query, criteriaBuilder)
                -> criteriaBuilder.conjunction();

        List<Book> books = List.of(
                createBook("Title1", "12345"),
                createBook("Title2", "67890")
        );
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(specificationBuilder.build(params)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return bookToDto(book);
        });
        List<BookDto> bookDtos = books.stream()
                .map(this::bookToDto)
                .toList();
        List<BookDto> response = bookService.search(params, pageable);

        assertEquals(bookDtos, response);
    }

    @DisplayName("Search books with invalid params")
    @Test
    public void search_InvalidParams_ReturnsEmptyList() {
        BookSearchParametersDto params = new BookSearchParametersDto();
        params.setAuthors(new String[]{"InvalidAuthor"});
        params.setTitles(new String[]{"InvalidTitle"});
        params.setPriceRange(new String[]{"1000", "2000"});

        Pageable pageable = PageRequest.of(0, 20);
        Specification<Book> bookSpecification = (root, query, criteriaBuilder)
                -> criteriaBuilder.disjunction();
        Page<Book> emptyBookPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(specificationBuilder.build(params)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification, pageable)).thenReturn(emptyBookPage);

        List<BookDto> response = bookService.search(params, pageable);
        assertTrue(response.isEmpty());
    }

    private Book createBook(String title, String isbn) {
        Book book = new Book();
        book.setAuthor("Author2");
        book.setIsbn(isbn);
        book.setPrice(BigDecimal.TEN);
        book.setTitle(title);
        book.setDescription("Description2");
        book.setCoverImage("coverImage2");
        book.setCategories(Set.of(new Category()));
        return book;
    }

    private BookDto bookToDto(Book book2) {
        BookDto bookDto = new BookDto();
        bookDto.setAuthor(book.getAuthor());
        bookDto.setPrice(book.getPrice());
        bookDto.setTitle(book.getTitle());
        bookDto.setDescription(book.getDescription());
        bookDto.setCoverImage(book.getCoverImage());
        bookDto.setCategoryIds(book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet()));
        return bookDto;
    }

    private Book updateBook(Book book, CreateBookRequestDto requestDto) {
        book.setAuthor(requestDto.getAuthor());
        book.setIsbn(requestDto.getIsbn());
        book.setTitle(requestDto.getTitle());
        book.setDescription(requestDto.getDescription());
        book.setPrice(requestDto.getPrice());
        book.setCategories(requestDto.getCategories().stream().map(categoryId -> {
            Category category = new Category();
            category.setId(categoryId);
            return category;
        }).collect(Collectors.toSet()));
        book.setCoverImage(requestDto.getCoverImage());
        return book;
    }

    private CreateBookRequestDto getCreateBookRequestDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setAuthor("Updated Author");
        requestDto.setPrice(BigDecimal.ONE);
        requestDto.setIsbn("234567");
        requestDto.setDescription("Updated description");
        requestDto.setCoverImage("Updated cover image");
        requestDto.setCategories(Set.of(1L));
        return requestDto;
    }
}
