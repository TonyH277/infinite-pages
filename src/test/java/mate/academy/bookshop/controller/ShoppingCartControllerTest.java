package mate.academy.bookshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.bookshop.BookshopApplication;
import mate.academy.bookshop.dto.shoppingcart.CartItemDto;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.UserRepository;
import mate.academy.bookshop.service.ShoppingCartService;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = BookshopApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    public static final Long NOT_EXISTING_ID = 999L;
    public static final Long VALID_BOOK_ID = 1L;
    public static final Long VALID_CART_ITEM_ID = 1L;
    public static final Long VALID_CART_ITEM_ID_2 = 2L;
    public static final String EMAIL = "user@example.com";
    public static final String FORBIDDEN_MESSAGE = "Forbidden";
    public static final String BOOK_TITLE_1 = "Book Title 1";
    public static final String BOOK_TITLE_2 = "Book Title 2";
    public static final String BOOK_TITLE_3 = "Book Title 3";
    public static final String REQUIRED_REQUEST_BODY_MISSING = "Required"
            + " request body is missing:"
            + " public mate.academy.bookshop.dto.shoppingcart.CartResponseDto"
            + " mate.academy.bookshop.controller.ShoppingCartController."
            + "addBookToShoppingCart(mate.academy.bookshop.dto.shoppingcart."
            + "CartItemRequestDto,org.springframework.security.core.Authentication)";

    private static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            teardown(dataSource);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/books/add-books.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/shopping-cart/tear-down.sql"));
        }
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        user = userRepository.findByEmail(EMAIL).orElseThrow();
        setAuthentication(user);
    }

    @SneakyThrows
    @AfterEach
    public void clearUp(@Autowired DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/shopping-cart/delete-all-cart-items.sql"));
        }
    }

    @Test
    @DisplayName("Get shopping cart for authenticated user")
    public void getShoppingCart_AuthenticatedUser_ReturnsCartResponseDto() throws Exception {
        CartResponseDto expected = shoppingCartService.getShoppingCart(user.getId());

        MvcResult result = mockMvc.perform(get("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        CartResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.constructType(CartResponseDto.class));

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get shopping cart for unauthenticated user")
    public void getShoppingCart_UnAuthenticatedUser_ThrowsForbiddenException() throws Exception {
        User unAuthenticatedUser = new User();
        MvcResult result = mockMvc.perform(get("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(unAuthenticatedUser)))
                .andExpect(status().isForbidden())
                .andReturn();
        String actual = result.getResponse().getErrorMessage();

        assertEquals(FORBIDDEN_MESSAGE, actual);
    }

    @Test
    @DisplayName("Add book to shopping cart with valid parameters")
    public void addBookToShoppingCart_ValidParams_ReturnsCartResponseDto()
            throws Exception {
        CartItemRequestDto requestDto = new CartItemRequestDto(VALID_BOOK_ID, 10);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CartItemDto cartItemDto = new CartItemDto(VALID_BOOK_ID, requestDto.bookId(),
                BOOK_TITLE_1, requestDto.quantity());
        CartResponseDto expected = new CartResponseDto(
                shoppingCartService.getShoppingCart(user.getId()).id(),
                user.getId(),
                Set.of(cartItemDto));

        MvcResult result = mockMvc.perform(post("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        CartResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CartResponseDto.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add book to shopping cart without request DTO")
    public void addBookToShoppingCart_WithoutRequestDto_ThrowsException() throws Exception {
        String expected = REQUIRED_REQUEST_BODY_MISSING;

        MvcResult result = mockMvc.perform(post("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add book to shopping cart with non-existing book ID")
    public void addBookToShoppingCart_NotExistingBookId_ReturnsCartResponseDto()
            throws Exception {
        CartItemRequestDto requestDto = new CartItemRequestDto(NOT_EXISTING_ID, 10);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "No book with id " + NOT_EXISTING_ID;

        MvcResult result = mockMvc.perform(post("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book quantity in shopping cart with valid request")
    @Sql(scripts = "classpath:database/shopping-cart/create-cart-items.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateBookQuantityByCartId_ValidRequest_ReturnsCartResponseDto()
            throws Exception {
        UpdateCartItemRequestDto requestDto = new UpdateCartItemRequestDto(11);
        Long cartItemId = VALID_CART_ITEM_ID;
        CartItemDto cartItemDto1 = new CartItemDto(VALID_CART_ITEM_ID, 3L, BOOK_TITLE_3,
                requestDto.quantity());
        CartItemDto cartItemDto2 = new CartItemDto(2L, 2L, BOOK_TITLE_2, 1);
        CartItemDto cartItemDto3 = new CartItemDto(3L, VALID_BOOK_ID, BOOK_TITLE_1, 3);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CartResponseDto expected = new CartResponseDto(
                shoppingCartService.getShoppingCart(user.getId()).id(),
                user.getId(),
                Set.of(cartItemDto1, cartItemDto2, cartItemDto3));

        MvcResult result = mockMvc.perform(put("/cart/cart-items/{cartItemId}", cartItemId)
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        CartResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CartResponseDto.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book quantity in shopping cart for unauthenticated user")
    public void updateBookQuantityByCartId_UnAuthenticatedUser_ThrowsForbiddenException()
            throws Exception {
        User unAuthenticatedUser = new User();
        UpdateCartItemRequestDto requestDto = new UpdateCartItemRequestDto(11);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/cart/cart-items/{cartItemId}", VALID_CART_ITEM_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(unAuthenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden())
                .andReturn();
        String actual = result.getResponse().getErrorMessage();

        assertEquals(FORBIDDEN_MESSAGE, actual);
    }

    @Test
    @DisplayName("Update book quantity in shopping cart with non-existing cart item ID")
    public void updateBookQuantityByCartId_NotExistingCartItemId_ReturnsCartResponseDto()
            throws Exception {
        UpdateCartItemRequestDto requestDto = new UpdateCartItemRequestDto(11);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "No cart item with id " + NOT_EXISTING_ID;

        MvcResult result = mockMvc.perform(put("/cart/cart-items/{cartItemId}", NOT_EXISTING_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete book from shopping cart with valid cart item ID")
    @Sql(scripts = "classpath:database/shopping-cart/create-cart-items.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteBookFromShoppingCart_ValidCartItemId_ReturnsCartResponseDto()
            throws Exception {
        CartItemDto cartItemDto1 = new CartItemDto(VALID_CART_ITEM_ID, 3L, BOOK_TITLE_3, 2);
        CartItemDto cartItemDto3 = new CartItemDto(3L, VALID_BOOK_ID, BOOK_TITLE_1, 3);
        CartResponseDto expected = new CartResponseDto(shoppingCartService
                .getShoppingCart(user.getId()).id(),
                user.getId(),
                Set.of(cartItemDto1, cartItemDto3));
        Long cartItemId = VALID_CART_ITEM_ID_2;

        mockMvc.perform(delete("/cart/cart-items/{cartItemId}", cartItemId)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isNoContent())
                .andReturn();

        MvcResult result = mockMvc.perform(get("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CartResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.constructType(CartResponseDto.class));

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete book from shopping cart with non-existing cart item ID")
    public void deleteBookFromShoppingCart_InvalidCartItemId_ThrowsEntityNotFoundException()
            throws Exception {
        String expected = "No cart item with id " + NOT_EXISTING_ID;
        MvcResult result = mockMvc.perform(delete("/cart/cart-items/{cartItemId}", NOT_EXISTING_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    private void setAuthentication(User user) {
        UserDetails userDetails = withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toArray(SimpleGrantedAuthority[]::new))
                .build();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
