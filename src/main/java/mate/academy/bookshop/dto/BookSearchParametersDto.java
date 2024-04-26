package mate.academy.bookshop.dto;

import lombok.Data;

@Data
public class BookSearchParametersDto {
    private String[] titles;
    private String[] authors;
    private String[] priceRange;
    private String[] isbns;
}
