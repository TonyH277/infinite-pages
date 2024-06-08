package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.dto.shoppingcart.CartItemDto;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.mapper.CartItemMapper;
import mate.academy.bookshop.mapper.ShoppingCartMapper;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.Category;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.repository.CartItemRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;
import mate.academy.bookshop.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    private static final String AUTHOR = "Author";
    private static final String ISBN = "2131231";
    private static final BigDecimal PRICE = BigDecimal.TEN;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String COVER_IMAGE = "coverImage";
    private static final Long USER_ID = 1L;
    private static final Long NOT_EXISTING_BOOK_ID = 100L;
    private static final Long NOT_EXISTING_USER_ID = 100L;
    private static final Long NOT_EXISTING_CART_ITEM_ID = 100L;
    private static final Long CART_ITEM_ID = 1L;
    private static final Long BOOK_ID = 1L;
    private static final int QUANTITY = 10;
    private static final int UPDATED_QUANTITY = 11;
    private static final String NO_CART_ITEM_WITH_ID_MSG = "No cart item with id ";
    private static final String NO_SHOPPING_CART_FOR_USER_ID_MSG = "No shopping cart for user id ";
    private static final String NO_BOOK_WITH_ID_MSG = "No book with id ";

    @Mock
    private BookRepository bookRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private Book book;
    private User user;
    private ShoppingCart shoppingCart;
    private CartItemRequestDto requestDto;
    private CartItemDto cartItemDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(USER_ID);

        book = new Book();
        book.setId(BOOK_ID);
        book.setAuthor(AUTHOR);
        book.setIsbn(ISBN);
        book.setPrice(PRICE);
        book.setTitle(TITLE);
        book.setDescription(DESCRIPTION);
        book.setCoverImage(COVER_IMAGE);
        book.setCategories(Set.of(new Category()));

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setCartItems(new HashSet<>());

        requestDto = new CartItemRequestDto(BOOK_ID, QUANTITY);
        cartItemDto = new CartItemDto(
                CART_ITEM_ID,
                book.getId(),
                book.getTitle(),
                requestDto.quantity());
    }

    @DisplayName("Get shopping cart by user id returns users shopping cart")
    @Test
    public void getShoppingCart_ReturnsCartResponseDto() {
        CartResponseDto expected = new CartResponseDto(
                CART_ITEM_ID,
                user.getId(),
                Set.of(cartItemDto));
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(any(ShoppingCart.class))).thenReturn(expected);

        CartResponseDto actual = shoppingCartService.getShoppingCart(user.getId());

        assertEquals(expected, actual);
    }

    @DisplayName("Get shopping cart invalid user throws EntityNotFoundException")
    @Test
    public void getShoppingCart_InvalidUserId_ReturnsCartResponseDto() {
        String expected = NO_SHOPPING_CART_FOR_USER_ID_MSG + NOT_EXISTING_USER_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> shoppingCartService.addBookToShoppingCart(requestDto, NOT_EXISTING_USER_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Add book to shopping cart")
    @Test
    public void addBookToShoppingCart_ValidRequestDto_ReturnsCartResponseDto() {
        CartItem cartItem = mapToEntity(requestDto, shoppingCart);
        CartResponseDto expected = new CartResponseDto(
                CART_ITEM_ID,
                user.getId(),
                Set.of(cartItemDto));
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(cartItemMapper.toEntity(requestDto)).thenReturn(cartItem);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(shoppingCart);
        when(shoppingCartMapper.toDto(any(ShoppingCart.class))).thenReturn(expected);

        CartResponseDto actual = shoppingCartService.addBookToShoppingCart(requestDto,
                user.getId());

        assertEquals(expected, actual);
    }

    @DisplayName("Add not existing book to shopping cart throws EntityNotFoundException")
    @Test
    public void addBookToShoppingCart_InvalidRequestDto_ThrowsEntityNotFoundException() {
        requestDto = new CartItemRequestDto(NOT_EXISTING_BOOK_ID, QUANTITY);
        CartItem cartItem = mapToEntity(requestDto, shoppingCart);
        Book bookWithNotExistingId = new Book();
        bookWithNotExistingId.setId(NOT_EXISTING_BOOK_ID);
        cartItem.setBook(bookWithNotExistingId);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemMapper.toEntity(requestDto)).thenReturn(cartItem);
        when(bookRepository.findById(NOT_EXISTING_BOOK_ID)).thenReturn(Optional.empty());
        String expected = NO_BOOK_WITH_ID_MSG + NOT_EXISTING_BOOK_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> shoppingCartService.addBookToShoppingCart(requestDto, user.getId()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Update book quantity by cart item id")
    @Test
    public void updateBookQuantityByCartItemId_ValidCartItemId_ReturnsCartResponseDto() {
        CartItem cartItem = new CartItem();
        cartItem.setId(CART_ITEM_ID);
        shoppingCart.setCartItems(Set.of(cartItem));
        UpdateCartItemRequestDto updateRequest = new UpdateCartItemRequestDto(UPDATED_QUANTITY);
        CartItemDto updatedCartItemDto = new CartItemDto(
                CART_ITEM_ID,
                book.getId(),
                book.getTitle(),
                updateRequest.quantity());
        CartResponseDto expected = new CartResponseDto(
                CART_ITEM_ID,
                user.getId(),
                Set.of(updatedCartItemDto));

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(any(ShoppingCart.class))).thenReturn(expected);

        CartResponseDto actual = shoppingCartService.updateBookQuantityByCartItemId(
                CART_ITEM_ID,
                user.getId(),
                updateRequest);

        assertEquals(expected, actual);
    }

    @DisplayName("Update book quantity by invalid cart item id throws EntityNotFoundException")
    @Test
    public void updateBookQuantityByCartItemId_InvalidCartItemId_ThrowsEntityNotFoundException() {
        String expected = NO_CART_ITEM_WITH_ID_MSG + NOT_EXISTING_CART_ITEM_ID;
        UpdateCartItemRequestDto updateRequest = new UpdateCartItemRequestDto(UPDATED_QUANTITY);
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> shoppingCartService.updateBookQuantityByCartItemId(
                NOT_EXISTING_CART_ITEM_ID,
                user.getId(),
                updateRequest));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Update book quantity by invalid user id throws EntityNotFoundException")
    @Test
    public void updateBookQuantityByCartItemId_InvalidUserId_ThrowsEntityNotFoundException() {
        String expected = NO_SHOPPING_CART_FOR_USER_ID_MSG + NOT_EXISTING_USER_ID;
        UpdateCartItemRequestDto updateRequest = new UpdateCartItemRequestDto(UPDATED_QUANTITY);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> shoppingCartService.updateBookQuantityByCartItemId(
                CART_ITEM_ID,
                NOT_EXISTING_USER_ID,
                updateRequest));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Delete book from shopping cart")
    @Test
    public void deleteBookFromShoppingCart_ValidCartItemId_DeleteBookFromShoppingCart() {
        when(cartItemRepository.existsById(CART_ITEM_ID)).thenReturn(true);

        shoppingCartService.deleteBookFromShoppingCart(CART_ITEM_ID);

        verify(cartItemRepository).deleteById(CART_ITEM_ID);
    }

    @DisplayName("Delete book from shopping cart by invalid shopping cart id throws"
            + " EntityNotFoundException")
    @Test
    public void deleteBookFromShoppingCart_InvalidCartItemId_ThrowsEntityNotFoundException() {
        Long invalidId = 100L;
        String expected = NO_CART_ITEM_WITH_ID_MSG + invalidId;
        when(cartItemRepository.existsById(any(Long.class))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> shoppingCartService.deleteBookFromShoppingCart(invalidId));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    private CartItem mapToEntity(CartItemRequestDto requestDto, ShoppingCart shoppingCart) {
        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setBook(book);
        cartItem.setQuantity(requestDto.quantity());
        return cartItem;
    }
}
