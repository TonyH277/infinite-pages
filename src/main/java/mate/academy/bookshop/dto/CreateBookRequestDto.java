package mate.academy.bookshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateBookRequestDto {
    @NotBlank(message = "can not be blank")
    @Size(max = 255, message = "max length is 255 characters")
    private String title;

    @NotBlank(message = "can not be blank")
    @Size(max = 255, message = "max length is 255 characters")
    private String author;

    @NotBlank(message = "can not be blank")
    @Size(min = 10, message = "min length is 10 characters")
    @Size(max = 255, message = "max length is 255 characters")
    private String isbn;

    @NotNull(message = "can not be null")
    @Min(value = 0, message = "min value is 0")
    private BigDecimal price;

    @NotBlank(message = "can not be blank")
    private String description;

    private String coverImage;
}

