package mate.academy.bookshop.service.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import mate.academy.bookshop.dto.BookDto;
import mate.academy.bookshop.dto.CreateBookRequestDto;
import mate.academy.bookshop.mapper.BookMapper;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.repository.BookRepository;
import mate.academy.bookshop.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {
    final private BookRepository bookRepository;

    @Override
    public Book save(CreateBookRequestDto requestDto) {
//        requestDto.
//        return bookRepository.save(book);
        return null;
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll();
    }
}
