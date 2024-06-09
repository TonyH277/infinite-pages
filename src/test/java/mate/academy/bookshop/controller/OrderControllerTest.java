package mate.academy.bookshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.bookshop.dto.order.OrderItemResponseDto;
import mate.academy.bookshop.dto.order.OrderRequestDto;
import mate.academy.bookshop.dto.order.OrderResponseDto;
import mate.academy.bookshop.dto.order.UpdateOrderDto;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.model.order.Status;
import mate.academy.bookshop.repository.UserRepository;
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
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest
        .WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

    public static final String EMAIL = "user@example.com";
    public static final String FORBIDDEN_MESSAGE = "Forbidden";
    public static final String ADDRESS = "Test address";
    public static final String PALACE_ORDER_VALIDATION_EXCEPTION_MESSAGE = "Required "
            + "request body is missing:"
            + " public mate.academy.bookshop.dto.order.OrderResponseDto "
            + "mate.academy.bookshop.controller.OrderController.placeOrder"
            + "(mate.academy.bookshop.dto.order.OrderRequestDto,org."
            + "springframework.security.core.Authentication)";
    public static final String UPDATE_ORDER_VALIDATION_EXCEPTION = "Required "
            + "request body is missing: "
            + "public mate.academy.bookshop.dto.order.OrderResponseDto mate"
            + ".academy.bookshop.controller.OrderController.updateOrder(java."
            + "lang.Long,mate.academy.bookshop.dto.order.UpdateOrderDto)";
    public static final long NON_EXISTING_ID = 999L;

    private static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

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
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/orders/clear-up-orders.sql"));
        }
    }

    @Test
    @DisplayName("Place order with valid parameters")
    @Sql(scripts = "classpath:database/shopping-cart/create-cart-items.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void placeOrder_ValidParams_ReturnsOrderResponseDto() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("Test address");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        OrderResponseDto expected = createOrderResponseDto();
        MvcResult result = mockMvc.perform(post("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        OrderResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                OrderResponseDto.class);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "orderDate", "orderItems"));
        assertEquals(expected.orderItems().size(), actual.orderItems().size());
        compareOrderItems(expected.orderItems(), actual.orderItems());
    }

    @Test
    @DisplayName("Place order with unauthenticated user")
    public void placeOrder_UnAuthenticatedUser_ThrowsForbiddenException() throws Exception {
        User unAuthenticatedUser = new User();
        OrderRequestDto requestDto = new OrderRequestDto(ADDRESS);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(unAuthenticatedUser))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
        String actual = result.getResponse().getErrorMessage();

        assertEquals(FORBIDDEN_MESSAGE, actual);
    }

    @Test
    @DisplayName("Place order without request body")
    public void placeOrder_WithoutRequestDto_ThrowsValidationException() throws Exception {
        String expected = PALACE_ORDER_VALIDATION_EXCEPTION_MESSAGE;
        MvcResult result = mockMvc.perform(post("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get orders for authenticated user")
    @Sql(scripts = "classpath:database/orders/create-orders.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getOrders_AuthenticatedUser_ReturnsListOfUserOrders() throws Exception {
        List<OrderResponseDto> expected = retrieveOrdersFormSqlScript();
        MvcResult result = mockMvc.perform(get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderResponseDto> actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class,
                        OrderResponseDto.class));

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        compareExpectedListToActualList(expected, actual);
    }

    @Test
    @DisplayName("Get orders with unauthenticated user")
    public void getOrders_UnAuthenticatedUser_ThrowsForbiddenException() throws Exception {
        User unAuthenticatedUser = new User();
        MvcResult result = mockMvc.perform(get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(unAuthenticatedUser)))
                .andExpect(status().isForbidden())
                .andReturn();
        String actual = result.getResponse().getErrorMessage();

        assertEquals(FORBIDDEN_MESSAGE, actual);
    }

    @Test
    @DisplayName("Update order with valid parameters")
    @Sql(scripts = "classpath:database/orders/create-orders.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateOrder_ValidParams_ReturnsOrderResponseDto() throws Exception {
        Long orderId = 1L;
        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow();
        setAuthentication(admin);
        UpdateOrderDto updateDto = new UpdateOrderDto(Status.DELIVERED.name());
        String jsonRequest = objectMapper.writeValueAsString(updateDto);
        List<OrderResponseDto> orderResponseDtos = retrieveOrdersFormSqlScript();
        OrderResponseDto expected = updateExpectedOrderById(orderResponseDtos, orderId);

        MvcResult result = mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                OrderResponseDto.class);

        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "orderDate", "orderItems"));

    }

    @Test
    @DisplayName("Update order without request body")
    public void updateOrder_WithoutRequestDto_ThrowsValidationException() throws Exception {
        Long orderId = 1L;
        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow();
        setAuthentication(admin);
        String expected = UPDATE_ORDER_VALIDATION_EXCEPTION;
        MvcResult result = mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get order items with valid order ID")
    @Sql(scripts = "classpath:database/orders/create-orders.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getOrderItems_ValidParams_ReturnsListOfOrderItems() throws Exception {
        Long orderId = 1L;
        List<OrderResponseDto> orderResponseDtos = retrieveOrdersFormSqlScript();
        List<OrderItemResponseDto> expected = orderResponseDtos.stream()
                .filter(o -> o.id().equals(orderId))
                .flatMap(o -> o.orderItems().stream())
                .toList();

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isOk())
                .andReturn();
        List<OrderItemResponseDto> actual = objectMapper
                .readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class,
                        OrderItemResponseDto.class));

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get order items with non-existing order ID")
    public void getOrderItems_NonExistingOrderId_ReturnsListOfOrderItems() throws Exception {
        String expected = "No order with id " + NON_EXISTING_ID;

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items", NON_EXISTING_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get specific order item with non-existing order ID")
    public void getOrderItemByOrderId_NonExistingOrderId_ThrowsEntityNotFoundException()
            throws Exception {
        String expected = "No order with id " + NON_EXISTING_ID;
        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items/{itemId}",
                        NON_EXISTING_ID,
                        1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get specific order item with non-existing item ID")
    @Sql(scripts = "classpath:database/orders/create-orders.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getOrderItemByOrderId_NonExistingItemId_ThrowsEntityNotFoundException()
            throws Exception {
        Long orderId = 1L;
        String expected = String
                .format("No item with id %d in order with id %d", NON_EXISTING_ID, orderId);

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items/{itemId}",
                        orderId,
                        NON_EXISTING_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResolvedException().getMessage();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get specific order item with valid parameters")
    @Sql(scripts = "classpath:database/orders/create-orders.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getOrderItemByOrderId_ValidParams_ReturnsOrderItemResponseDto() throws Exception {
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemResponseDto expected = retrieveOrdersFormSqlScript().stream()
                .filter(o -> o.id().equals(orderId))
                .flatMap(o -> o.orderItems().stream())
                .filter(i -> i.id().equals(itemId))
                .findFirst()
                .orElse(null);

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items/{itemId}",
                        orderId,
                        itemId)
                        .with(SecurityMockMvcRequestPostProcessors.user(user)))
                .andExpect(status().isOk())
                .andReturn();
        OrderItemResponseDto actual = objectMapper.readValue(result
                        .getResponse().getContentAsString(),
                OrderItemResponseDto.class);

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

    private void compareOrderItems(Set<OrderItemResponseDto> expected,
                                   Set<OrderItemResponseDto> actual) {
        assertTrue(expected.stream()
                .allMatch(expectedItem -> actual.stream()
                        .anyMatch(actualItem ->
                                EqualsBuilder.reflectionEquals(expectedItem,
                                        actualItem,
                                        "id"))));
    }

    private OrderResponseDto createOrderResponseDto() {

        OrderItemResponseDto itemResponseDto1 = new OrderItemResponseDto(
                1L,
                3L,
                BigDecimal.valueOf(12.99),
                2
        );

        OrderItemResponseDto itemResponseDto2 = new OrderItemResponseDto(
                2L,
                2L,
                BigDecimal.valueOf(11.99),
                1
        );

        OrderItemResponseDto itemResponseDto3 = new OrderItemResponseDto(
                3L,
                1L,
                BigDecimal.valueOf(10.99),
                3
        );

        OrderResponseDto responseDto = new OrderResponseDto(
                1L,
                user.getId(),
                Set.of(itemResponseDto1, itemResponseDto2, itemResponseDto3),
                LocalDateTime.now(),
                BigDecimal.valueOf(12.99 * 2 + 10.99 * 3 + 11.99),
                Status.PENDING.name()
        );
        return responseDto;
    }

    private void compareExpectedListToActualList(List<OrderResponseDto> expected,
                                                 List<OrderResponseDto> actual) {
        for (int i = 0; i < expected.size(); i++) {
            OrderResponseDto expectedOrder = expected.get(i);
            OrderResponseDto actualOrder = actual.get(i);

            assertTrue(EqualsBuilder.reflectionEquals(expectedOrder, actualOrder,
                    "orderDate", "orderItems"));
            compareOrderItems(expectedOrder.orderItems(), actualOrder.orderItems());
        }
    }

    private List<OrderResponseDto> retrieveOrdersFormSqlScript() {
        OrderItemResponseDto itemResponseDto1 = new OrderItemResponseDto(
                1L,
                1L,
                BigDecimal.valueOf(10.99),
                2
        );
        OrderItemResponseDto itemResponseDto2 = new OrderItemResponseDto(
                2L,
                2L,
                BigDecimal.valueOf(11.99),
                3
        );
        OrderResponseDto responseDto1 = new OrderResponseDto(
                1L,
                user.getId(),
                Set.of(itemResponseDto1, itemResponseDto2),
                LocalDateTime.now(),
                BigDecimal.valueOf(57.95),
                Status.PENDING.name()
        );

        OrderItemResponseDto itemResponseDto3 = new OrderItemResponseDto(
                3L,
                3L,
                BigDecimal.valueOf(12.99),
                1
        );
        OrderItemResponseDto itemResponseDto4 = new OrderItemResponseDto(
                4L,
                4L,
                BigDecimal.valueOf(13.99),
                4
        );

        OrderResponseDto responseDto2 = new OrderResponseDto(
                2L,
                user.getId(),
                Set.of(itemResponseDto3, itemResponseDto4),
                LocalDateTime.now(),
                BigDecimal.valueOf(68.95),
                Status.COMPLETED.name()
        );
        return List.of(responseDto1, responseDto2);
    }

    private OrderResponseDto updateExpectedOrderById(List<OrderResponseDto> expected,
                                                     Long orderId) {
        return expected.stream()
                .filter(o -> o.id().equals(orderId))
                .map(o -> new OrderResponseDto(
                        o.id(),
                        o.userId(),
                        o.orderItems(),
                        o.orderDate(),
                        o.total(),
                        Status.DELIVERED.name()))
                .findFirst()
                .orElse(null);
    }
}
