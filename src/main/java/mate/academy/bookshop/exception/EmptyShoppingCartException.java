package mate.academy.bookshop.exception;

public class EmptyShoppingCartException extends RuntimeException {

    public EmptyShoppingCartException(String message) {
        super(message);
    }
}
