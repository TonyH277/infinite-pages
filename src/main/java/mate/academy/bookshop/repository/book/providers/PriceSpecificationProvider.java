package mate.academy.bookshop.repository.book.providers;

import java.math.BigDecimal;
import mate.academy.bookshop.exception.InvalidPriceRangeException;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {
    private static final String PRICE = "price";
    private static final int EXPECTED_LENGTH = 2;
    private static final int MIN_PRICE_INDEX = 0;
    private static final int MAX_PRICE_INDEX = 1;

    @Override
    public String getKey() {
        return PRICE;
    }

    @Override
    public Specification<Book> getSpecification(String[] params) {
        try {
            if (params == null || params.length != EXPECTED_LENGTH) {
                throw new InvalidPriceRangeException("Price range should contain strict 2 params");
            }
            BigDecimal minPrice = BigDecimal.valueOf(Long.parseLong(params[MIN_PRICE_INDEX]));
            BigDecimal maxPrice = BigDecimal.valueOf(Long.parseLong(params[MAX_PRICE_INDEX]));

            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.between(root.get(PRICE), minPrice, maxPrice);
        } catch (NumberFormatException e) {
            throw new InvalidPriceRangeException("Price range should contain only numbers");
        }
    }
}
