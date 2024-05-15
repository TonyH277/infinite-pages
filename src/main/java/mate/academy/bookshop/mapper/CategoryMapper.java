package mate.academy.bookshop.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.category.CategoryRequestDto;
import mate.academy.bookshop.dto.category.CategoryResponseDto;
import mate.academy.bookshop.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryResponseDto toDto(Category category);

    Category toEntity(CategoryRequestDto categoryDto);

    @Named("categoriesById")
    default Set<Category> categoriesByIds(Set<Long> categories) {
        return categories.stream()
                .map(Category::new)
                .collect(Collectors.toSet());
    }
}
