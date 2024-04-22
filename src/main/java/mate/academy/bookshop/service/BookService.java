package mate.academy.bookshop.service;

import java.util.List;
import mate.academy.bookshop.dto.BookDto;
import mate.academy.bookshop.dto.CreateBookRequestDto;
import mate.academy.bookshop.model.Book;

public interface BookService {
    Book save(CreateBookRequestDto requestDto);

    List<BookDto> findAll();
}
