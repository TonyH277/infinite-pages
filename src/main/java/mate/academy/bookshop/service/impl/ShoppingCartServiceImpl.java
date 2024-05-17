package mate.academy.bookshop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.mapper.CartItemMapper;
import mate.academy.bookshop.mapper.ShoppingCartMapper;
import mate.academy.bookshop.model.CartItem;
import mate.academy.bookshop.model.ShoppingCart;
import mate.academy.bookshop.repository.CartItemRepository;
import mate.academy.bookshop.repository.ShoppingCartRepository;
import mate.academy.bookshop.repository.UserRepository;
import mate.academy.bookshop.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartResponseDto getShoppingCart(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public CartResponseDto addBookToShoppingCart(CartItemRequestDto requestDto, Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);
        CartItem cartItem = cartItemMapper.toEntity(requestDto);
        cartItem.setShoppingCart(shoppingCart);
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        shoppingCart.getCartItems().add(savedCartItem);
        ShoppingCart savedShoppingCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(savedShoppingCart);
    }

    @Override
    @Transactional
    public CartResponseDto updateBookQuantityByCartItemId(Long cartItemId,
                                                          Long userId,
                                                          UpdateCartItemRequestDto requestDto) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);
        CartItem cartItem = shoppingCart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No cart item with id "
                        + cartItemId));
        cartItem.setQuantity(requestDto.quantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public void deleteBookFromShoppingCart(Long cartItemId) {
        if (cartItemRepository.existsById(cartItemId)) {
            cartItemRepository.deleteById(cartItemId);
        } else {
            throw new EntityNotFoundException("No cart item with id " + cartItemId);
        }
    }
}
