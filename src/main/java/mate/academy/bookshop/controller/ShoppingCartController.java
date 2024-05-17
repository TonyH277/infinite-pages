package mate.academy.bookshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.shoppingcart.CartItemRequestDto;
import mate.academy.bookshop.dto.shoppingcart.CartResponseDto;
import mate.academy.bookshop.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.bookshop.model.User;
import mate.academy.bookshop.service.ShoppingCartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart management",
        description = "Endpoints for managing shopping carts"
                + "credentials for test user = 'user@example.com', 'user'")
@RestController
@RequiredArgsConstructor
@RequestMapping("cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @Operation(summary = "Get users shopping cart",
            description = "Get users shopping cart")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public CartResponseDto getShoppingCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.getShoppingCart(user.getId());
    }

    @Operation(summary = "Add book to user shopping cart",
            description = "Add book to user shopping cart")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public CartResponseDto addBookToShoppingCart(@RequestBody @Valid CartItemRequestDto requestDto,
                                                 Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.addBookToShoppingCart(requestDto, user.getId());
    }

    @Operation(summary = "Update book quantity at user shopping cart",
            description = "Update book quantity at user shopping cart")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/cart-items/{cartItemId}")
    public CartResponseDto updateBookQuantityByCartId(@PathVariable Long cartItemId,
                                                      Authentication authentication,
                                                      @RequestBody @Valid
                                                              UpdateCartItemRequestDto
                                                              requestDto) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.updateBookQuantityByCartItemId(cartItemId,
                user.getId(),
                requestDto);
    }

    @Operation(summary = "Delete book at user shopping cart",
            description = "Delete book at user shopping cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/cart-items/{cartItemId}")
    public void deleteBookFromShoppingCart(@PathVariable Long cartItemId) {
        shoppingCartService.deleteBookFromShoppingCart(cartItemId);
    }
}
