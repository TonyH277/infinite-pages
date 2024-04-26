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
    private final SpecificationProviderManager<Book> bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = Specification.where(null);
        if (searchParameters.getAuthors() != null
                && searchParameters.getAuthors().length > 0) {
            specification = specification.and(bookSpecificationProviderManager
                    .getSpecificationProvider("author")
                    .getSpecification(searchParameters.getAuthors()));
        }
        if (searchParameters.getTitles() != null
                && searchParameters.getTitles().length > 0) {
            specification = specification.and(bookSpecificationProviderManager
                    .getSpecificationProvider("title")
                    .getSpecification(searchParameters.getTitles()));
        }
        if (searchParameters.getPriceRange() != null
                && searchParameters.getPriceRange().length > 0) {
            specification = specification.and(bookSpecificationProviderManager
                    .getSpecificationProvider("price")
                    .getSpecification(searchParameters.getPriceRange()));
        }
        if (searchParameters.getIsbns() != null
                && searchParameters.getIsbns().length > 0) {
            specification = specification.and(bookSpecificationProviderManager
                    .getSpecificationProvider("isbn")
                    .getSpecification(searchParameters.getIsbns()));
        }
        return specification;
    }
}
