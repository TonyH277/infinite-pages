package mate.academy.bookshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.bookshop.dto.book.BookDto;
import mate.academy.bookshop.dto.book.BookSearchParametersDto;
import mate.academy.bookshop.dto.book.CreateBookRequestDto;
import mate.academy.bookshop.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book management", description = "Endpoints for managing books"
        + "credentials for test user = 'user@example.com', 'user' "
        + "credentials for test admin = 'admin@example.com', 'admin'")
@RequiredArgsConstructor
@RestController
@RequestMapping("books")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "Get all books",
            description = "Get certain page of books with custom size")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public List<BookDto> getAll(Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @Operation(summary = "Get book by id",
            description = "Get book by id")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}")
    public BookDto getById(@PathVariable Long id) {
        return bookService.findBookById(id);
    }

    @Operation(summary = "Get books which match searchParameters",
            description = "Get books which match searchParameters")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/search")
    public List<BookDto> searchBooks(BookSearchParametersDto searchParameters, Pageable pageable) {
        return bookService.search(searchParameters, pageable);
    }

    @Operation(summary = "Create a new book",
            description = "Create a new book")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BookDto create(@RequestBody @Valid CreateBookRequestDto requestDto) {
        return bookService.save(requestDto);
    }

    @Operation(summary = "Update book by id",
            description = "Update book by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BookDto updateById(@RequestBody CreateBookRequestDto requestDto, @PathVariable Long id) {
        return bookService.update(id, requestDto);
    }

    @Operation(summary = "Delete book by id",
            description = "Delete book by id")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        bookService.deleteById(id);
    }
}
