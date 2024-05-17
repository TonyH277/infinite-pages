package mate.academy.bookshop.dto.book;

import lombok.Data;

@Data
public class BookSearchParametersDto {
    private String[] titles;
    private String[] authors;
    private String[] priceRange;
    private String[] isbns;
}
