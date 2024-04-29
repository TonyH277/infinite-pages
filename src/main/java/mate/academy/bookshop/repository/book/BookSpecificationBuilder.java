package mate.academy.bookshop.repository.book;

import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.BookSearchParametersDto;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.repository.SpecificationBuilder;
import mate.academy.bookshop.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private static final String AUTHOR = "author";
    private static final String TITLE = "title";
    private static final String PRICE = "price";
    private static final String ISBN = "isbn";
    private final SpecificationProviderManager<Book> bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = Specification.where(null);
        specification = addSpecification(specification, searchParameters.getAuthors(), AUTHOR);
        specification = addSpecification(specification, searchParameters.getTitles(), TITLE);
        specification = addSpecification(specification, searchParameters.getPriceRange(), PRICE);
        specification = addSpecification(specification, searchParameters.getIsbns(), ISBN);
        return specification;
    }

    private Specification<Book> addSpecification(Specification<Book> specification,
                                                 String[] values, String field) {
        if (values != null && values.length > 0) {
            specification = specification.and(bookSpecificationProviderManager
                    .getSpecificationProvider(field)
                    .getSpecification(values));
        }
        return specification;
    }
}
