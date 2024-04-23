package mate.academy.bookshop.dto;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String description;
    private String coverImage;
}
