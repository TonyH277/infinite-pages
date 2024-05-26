package mate.academy.bookshop.controller;

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
import org.junit.jupiter.api.Assertions;
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
                    new ClassPathResource("database/categories/clear-categories.sql"));
        }
    }

    @BeforeEach
    void setup(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/categories/add-categories.sql"));
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
        MvcResult result = mockMvc.perform(get("/categories")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<CategoryResponseDto> actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class,
                                CategoryResponseDto.class));
        List<CategoryResponseDto> expected = getExpectedCategories();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(EqualsBuilder
                    .reflectionEquals(expected.get(i), actual.get(i), "id"));
        }
    }

    @DisplayName("Get category by ID when category exists")
    @WithMockUser(username = "user")
    @Test
    void getById_ExistingCategoryId_ReturnsCategory() throws Exception {
        Long id = 1L;

        MvcResult result = mockMvc.perform(get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        List<CategoryResponseDto> categoryResponseDtos = getExpectedCategories();
        CategoryResponseDto expected = categoryResponseDtos.get(0);

        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @DisplayName("Get books by category ID when category exists")
    @WithMockUser(username = "user")
    @Test
    void getBooksByCategoryId_ExistingCategoryId_ReturnsPageOfBooks() throws Exception {
        Long id = 1L;
        List<BookDtoWithoutCategoryIds> expected = getBooksWithoutCategories();
        MvcResult result = mockMvc.perform(get("/categories/{id}/books", id)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<BookDtoWithoutCategoryIds> actual = objectMapper.readValue(result
                        .getResponse()
                        .getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, BookDtoWithoutCategoryIds.class));

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(EqualsBuilder
                    .reflectionEquals(expected.get(i), actual.get(i), "id"));
        }
    }

    @DisplayName("Add new category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void add_NewCategory_CreatesCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Fiction",
                "Fiction description");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/categories")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        CategoryResponseDto expected = new CategoryResponseDto(1L, "Fiction",
                "Fiction description");

        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @DisplayName("Update existing category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void update_ExistingCategory_UpdatesCategory() throws Exception {
        Long id = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("Fiction",
                "Fiction description");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/categories/{id}", id)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                CategoryResponseDto.class);
        CategoryResponseDto expected = new CategoryResponseDto(1L, "Fiction",
                "Fiction description");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @DisplayName("Delete existing category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void delete_ExistingCategory_DeletesCategory() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String actual = result.getResolvedException().getMessage();
        String expected = "Category not found with id: " + id;
        Assertions.assertEquals(expected, actual);
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
