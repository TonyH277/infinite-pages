package mate.academy.bookshop.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BookSearchParametersDto {
    private String[] titles;
    private String[] authors;
    private BigDecimal price;
    private String description;
}
