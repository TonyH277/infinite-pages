package mate.academy.bookshop.repository.bookSpecificaiton;

import java.util.Arrays;
import mate.academy.bookshop.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class PriceSpecification {
    Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> root.get("price").in(Arrays.stream(params).toArray());
    }
}
