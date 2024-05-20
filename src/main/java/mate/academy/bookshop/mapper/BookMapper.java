package mate.academy.bookshop.mapper;

import java.util.stream.Collectors;
import mate.academy.bookshop.config.MapperConfig;
import mate.academy.bookshop.dto.book.BookDto;
import mate.academy.bookshop.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.bookshop.dto.book.CreateBookRequestDto;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Category;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = CategoryMapper.class)
public interface BookMapper {

    @Mapping(target = "id", source = "book.id")
    BookDto toDto(Book book);

    @Mapping(target = "categories", source = "categories", qualifiedByName = "categoriesById")
    Book toModel(CreateBookRequestDto requestDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        bookDto.setCategoryIds(book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet()));
    }

    @Named("bookFromId")
    default Book bookFromId(Long id) {
        Book book = new Book();

        book.setId(id);
        return book;
    }
}
