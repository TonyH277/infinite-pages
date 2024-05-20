package mate.academy.bookshop.service;

import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.dto.shoppingcart.UpdateCartItemRequestDto;

public interface ShoppingCartService {

    CartResponseDto getShoppingCart(Long userId);

    CartResponseDto addBookToShoppingCart(CartItemRequestDto requestDto, Long userId);

    CartResponseDto updateBookQuantityByCartItemId(Long cartItemId,
                                                   Long userId,
                                                   UpdateCartItemRequestDto requestDto);

    void deleteBookFromShoppingCart(Long cartItemId);
}
