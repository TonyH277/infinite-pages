package mate.academy.bookshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.bookshop.dto.book.BookDto;
import mate.academy.bookshop.dto.book.CreateBookRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {

    private static final String BOOK_TITLE = "Book Title ";
    private static final String BOOK_AUTHOR = "Author ";
    private static final Double BOOK_PRICE = 10.99;
    private static final String BOOK_DESCRIPTION = "Description for Book ";
    private static final String BOOK_IMAGE = "coverImage";
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext,
                          @Autowired DataSource dataSource) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            teardown(dataSource);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/books/add-books.sql"));
        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/books/remove-books.sql"));
        }
    }

    @AfterEach
    void clearUp(
            @Autowired DataSource dataSource
    ) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/books/clear-up-tables.sql"));
        }
    }

    @DisplayName("Get all books with default pageable params")
    @WithMockUser(username = "user")
    @Test
    void getAll_DefaultPageableParams_ReturnsPageOfBooks() throws Exception {
        List<BookDto> expected = getExpectedBooks();
        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookDto.class));

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Get all books with custom pageable params")
    @WithMockUser(username = "user")
    @Test
    void getAll_CustomPageableParams_ReturnsCustomPageOfBooks() throws Exception {
        List<BookDto> bookDtos = getExpectedBooks();
        List<BookDto> expected = List.of(bookDtos.get(5), bookDtos.get(6));

        MvcResult result = mockMvc.perform(get("/books")
                        .param("page", "2")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookDto.class));

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Get book by ID when book exists")
    @WithMockUser(username = "user")
    @Test
    void getById_ValidId_ReturnsBookDto() throws Exception {
        Long id = 4L;
        int expectedBookIndex = 3;
        List<BookDto> expected = getExpectedBooks();

        MvcResult result = mockMvc.perform(get("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);

        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected.get(expectedBookIndex),
                actual, "id"));
    }

    @DisplayName("Get book by ID with invalid ID")
    @WithMockUser(username = "user")
    @Test
    void getById_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = 24L;
        String expected = "Can't find book with id " + invalidId;

        MvcResult result = mockMvc.perform(get("/books/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Search books with valid parameters and default pageable")
    @WithMockUser(username = "user")
    @Test
    void searchBooks_ValidParametersDefaultPageable_ReturnsPageOfBooks() throws Exception {
        List<BookDto> bookDtos = getExpectedBooks();
        List<BookDto> expected = List.of(bookDtos.get(0), bookDtos.get(1), bookDtos.get(2));

        MvcResult result = mockMvc.perform(get("/books/search")
                        .param("authors", "Author 1, Author 2, Author 3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookDto.class));

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Search books with valid parameters and custom pageable")
    @WithMockUser(username = "user")
    @Test
    void searchBooks_ValidParametersCustomPageable_ReturnsCustomPageOfBooks() throws Exception {
        List<BookDto> bookDtos = getExpectedBooks();
        List<BookDto> expected = bookDtos.stream()
                .filter(bookDto -> bookDto.getAuthor().matches("Author [1-5]"))
                .skip(3)
                .limit(3)
                .toList();

        MvcResult result = mockMvc.perform(get("/books/search")
                        .param("page", "1")
                        .param("size", "3")
                        .param("authors", "Author 1, Author 2, Author 3, Author 4, Author 5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookDto.class));

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Create new book with valid request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void create_ValidRequestDto_ReturnsBookResponseDto() throws Exception {
        CreateBookRequestDto newBook = getRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(newBook);
        BookDto expected = toBookDto(newBook);

        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);

        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @DisplayName("Create new book with invalid request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void create_InvalidRequestDto_ThrowsValidationException() throws Exception {
        CreateBookRequestDto newBook = new CreateBookRequestDto();
        newBook.setTitle("New Book");
        String jsonRequest = objectMapper.writeValueAsString(newBook);

        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertTrue(actual.contains("Validation failed"));
    }

    @DisplayName("Update book by ID with valid ID and request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void updateById_ValidIdAndValidRequestDto_ReturnsBookResponseDto() throws Exception {
        CreateBookRequestDto updatedBook = getRequestDto();
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(updatedBook);
        BookDto expected = toBookDto(updatedBook);

        MvcResult result = mockMvc.perform(put("/books/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);

        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @DisplayName("Update book by ID with invalid ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void updateById_InvalidId_ThrowsValidationException() throws Exception {
        CreateBookRequestDto updatedBook = getRequestDto();
        Long invalidId = 24L;
        String jsonRequest = objectMapper.writeValueAsString(updatedBook);
        String expected = "Can't find book with id " + invalidId;

        MvcResult result = mockMvc.perform(put("/books/{id}", invalidId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Delete book by ID with valid ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void deleteById_ValidId_Success() throws Exception {
        Long id = 4L;
        String expected = "Can't find book with id " + id;
        mockMvc.perform(delete("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MvcResult result = mockMvc.perform(get("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Delete book by ID with invalid ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void deleteById_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = 24L;
        String expected = "Can't find book with id " + invalidId;

        MvcResult result = mockMvc.perform(delete("/books/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertEquals(expected, actual);

    }

    private List<BookDto> getExpectedBooks() {
        return IntStream.rangeClosed(1, 20)
                .mapToObj(i -> {
                    BookDto bookDto = new BookDto();
                    bookDto.setTitle(BOOK_TITLE + i);
                    bookDto.setAuthor(BOOK_AUTHOR + i);
                    bookDto.setPrice(BigDecimal.valueOf(BOOK_PRICE + i - 1)
                            .setScale(2, RoundingMode.HALF_UP));
                    bookDto.setDescription(BOOK_DESCRIPTION + i);
                    bookDto.setCoverImage(BOOK_IMAGE + i + ".jpg");
                    bookDto.setCategoryIds(Set.of(1L));
                    return bookDto;
                })
                .collect(Collectors.toList());
    }

    private BookDto toBookDto(CreateBookRequestDto createBookRequestDto) {
        BookDto bookDto = new BookDto();
        bookDto.setTitle(createBookRequestDto.getTitle());
        bookDto.setAuthor(createBookRequestDto.getAuthor());
        bookDto.setPrice(createBookRequestDto.getPrice());
        bookDto.setDescription(createBookRequestDto.getDescription());
        bookDto.setCoverImage(createBookRequestDto.getCoverImage());
        bookDto.setCategoryIds(createBookRequestDto.getCategories());
        return bookDto;
    }

    private CreateBookRequestDto getRequestDto() {
        CreateBookRequestDto updatedBook = new CreateBookRequestDto();
        updatedBook.setTitle("New Book");
        updatedBook.setAuthor("New Author");
        updatedBook.setPrice(BigDecimal.valueOf(15.99));
        updatedBook.setDescription("New Description");
        updatedBook.setCoverImage("newCoverImage.jpg");
        updatedBook.setCategories(Set.of(1L));
        updatedBook.setIsbn("67890-236589");
        return updatedBook;
    }

    private void compareExpectedListToActualList(List<?> expected, List<?> actual) {
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(EqualsBuilder
                    .reflectionEquals(expected.get(i), actual.get(i), "id"));
        }
    }
}
