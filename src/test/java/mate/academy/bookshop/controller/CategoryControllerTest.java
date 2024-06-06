package mate.academy.bookshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.bookshop.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.bookshop.dto.category.CategoryRequestDto;
import mate.academy.bookshop.dto.category.CategoryResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
class CategoryControllerTest {

    private static final String CATEGORY_BASE_URL = "/categories";
    private static final String CATEGORY_BOOKS_URL = CATEGORY_BASE_URL + "/{id}/books";
    private static final String CATEGORY_ID_URL = CATEGORY_BASE_URL + "/{id}";
    private static final String CLEAR_CATEGORIES_SCRIPT
            = "database/categories/clear-categories.sql";
    private static final String ADD_CATEGORIES_SCRIPT = "database/categories/add-categories.sql";
    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found with id: ";
    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON.toString();
    private static final String CATEGORY_NAME = "Fiction";
    private static final String CATEGORY_DESCRIPTION = "Fiction description";
    private static final Long TEST_CATEGORY_ID = 1L;

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext,
                          @Autowired DataSource dataSource) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(CLEAR_CATEGORIES_SCRIPT));
        }
    }

    @BeforeEach
    void setup(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(ADD_CATEGORIES_SCRIPT));
        }
    }

    @AfterEach
    void cleanup(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @DisplayName("Get all categories with default pageable params")
    @WithMockUser(username = "user")
    @Test
    void getAll_DefaultPageableParams_ReturnsPageOfCategories() throws Exception {
        MvcResult result = mockMvc.perform(get(CATEGORY_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<CategoryResponseDto> actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, CategoryResponseDto.class));
        List<CategoryResponseDto> expected = getExpectedCategories();

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Get category by ID when category exists")
    @WithMockUser(username = "user")
    @Test
    void getById_ExistingCategoryId_ReturnsCategory() throws Exception {
        MvcResult result = mockMvc.perform(get(CATEGORY_ID_URL, TEST_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        List<CategoryResponseDto> categoryResponseDtos = getExpectedCategories();

        CategoryResponseDto expected = categoryResponseDtos.get(0);
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @DisplayName("Get books by category ID when category exists")
    @WithMockUser(username = "user")
    @Test
    void getBooksByCategoryId_ExistingCategoryId_ReturnsPageOfBooks() throws Exception {
        List<BookDtoWithoutCategoryIds> expected = getBooksWithoutCategories();
        MvcResult result = mockMvc.perform(get(CATEGORY_BOOKS_URL, TEST_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<BookDtoWithoutCategoryIds> actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, BookDtoWithoutCategoryIds.class));

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @DisplayName("Add new category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void add_NewCategory_CreatesCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post(CATEGORY_BASE_URL)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        CategoryResponseDto expected = new CategoryResponseDto(TEST_CATEGORY_ID,
                CATEGORY_NAME,
                CATEGORY_DESCRIPTION);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @DisplayName("Update existing category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void update_ExistingCategory_UpdatesCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put(CATEGORY_ID_URL, TEST_CATEGORY_ID)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        CategoryResponseDto expected = new CategoryResponseDto(TEST_CATEGORY_ID,
                CATEGORY_NAME,
                CATEGORY_DESCRIPTION);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @DisplayName("Delete existing category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void delete_ExistingCategory_DeletesCategory() throws Exception {
        mockMvc.perform(delete(CATEGORY_ID_URL, TEST_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get(CATEGORY_ID_URL, TEST_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String actual = result.getResolvedException().getMessage();
        String expected = CATEGORY_NOT_FOUND_MSG + TEST_CATEGORY_ID;
        assertEquals(expected, actual);
    }

    private List<CategoryResponseDto> getExpectedCategories() {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> {
                    CategoryResponseDto categoryDto = new CategoryResponseDto(Long.valueOf(i),
                            "Category " + i, "Description for Category " + i);
                    return categoryDto;
                })
                .collect(Collectors.toList());
    }

    private void compareExpectedListToActualList(List<?> expected, List<?> actual) {
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(EqualsBuilder
                    .reflectionEquals(expected.get(i), actual.get(i), "id"));
        }
    }

    private List<BookDtoWithoutCategoryIds> getBooksWithoutCategories() {
        return IntStream.rangeClosed(1, 20)
                .mapToObj(i -> {
                    BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds =
                            new BookDtoWithoutCategoryIds();
                    bookDtoWithoutCategoryIds.setTitle("Book Title " + i);
                    bookDtoWithoutCategoryIds.setAuthor("Author " + i);
                    bookDtoWithoutCategoryIds.setPrice(BigDecimal.valueOf(10.99 + i - 1)
                            .setScale(2, RoundingMode.HALF_UP));
                    bookDtoWithoutCategoryIds.setDescription("Description for Book " + i);
                    bookDtoWithoutCategoryIds.setCoverImage("coverImage" + i + ".jpg");
                    return bookDtoWithoutCategoryIds;
                })
                .toList();
    }
}
