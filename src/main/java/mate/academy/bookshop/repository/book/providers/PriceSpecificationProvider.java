package mate.academy.bookshop.repository.book.providers;

import java.math.BigDecimal;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {

    @Override
    public String getKey() {
        return "price";
    }

    @Override
    public Specification<Book> getSpecification(String[] params) {
        if (params == null || params.length != 2) {
            throw new IllegalArgumentException("Invalid price range parameters");
        }
        BigDecimal minPrice = BigDecimal.valueOf(Long.parseLong(params[0]));
        BigDecimal maxPrice = BigDecimal.valueOf(Long.parseLong(params[1]));

        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
    }
}
