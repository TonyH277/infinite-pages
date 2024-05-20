package mate.academy.bookshop.dto.shoppingcart;

public record CartItemRequestDto(
        Long bookId,
        int quantity
) {
}
