package mate.academy.bookshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookshop.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.bookshop.dto.category.CategoryRequestDto;
import mate.academy.bookshop.dto.category.CategoryResponseDto;
import mate.academy.bookshop.exception.EntityNotFoundException;
import mate.academy.bookshop.mapper.BookMapper;
import mate.academy.bookshop.mapper.CategoryMapper;
import mate.academy.bookshop.model.Book;
import mate.academy.bookshop.model.Category;
import mate.academy.bookshop.repository.CategoryRepository;
import mate.academy.bookshop.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    private static final long CATEGORY_ID = 1L;
    private static final long NOT_EXISTING_CATEGORY_ID = 2L;
    private static final String CATEGORY_NAME = "Category1";
    private static final String CATEGORY_DESCRIPTION = "Description";
    private static final String UPDATED_CATEGORY_NAME = "Category2";
    private static final String UPDATED_CATEGORY_DESCRIPTION = "Description2";
    private static final String EXPECTED_CATEGORY_NOT_FOUND_MESSAGE
            = "Category not found with id: ";
    private static final String AUTHOR = "Author";
    private static final BigDecimal PRICE = BigDecimal.TEN;
    private static final String TITLE1 = "Title1";
    private static final String ISBN1 = "234567";
    private static final String TITLE2 = "Title2";
    private static final String ISBN2 = "234560";
    private static final String TITLE3 = "Title3";
    private static final String ISBN3 = "234599";
    private static final String COVER_IMAGE = "coverImage";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponseDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(CATEGORY_NAME);
        category.setDescription(CATEGORY_DESCRIPTION);

        categoryDto = new CategoryResponseDto(CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESCRIPTION);
    }

    @DisplayName("Save new category")
    @Test
    public void save_NewCategory_ReturnsCategoryResponseDto() {
        CategoryRequestDto requestDto = new CategoryRequestDto(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryResponseDto response = categoryService.save(requestDto);

        assertEquals(categoryDto, response);
    }

    @DisplayName("Update existing category")
    @Test
    public void update_ExistingCategory_ReturnsCategoryResponseDto() {
        CategoryRequestDto requestDto = new CategoryRequestDto(UPDATED_CATEGORY_NAME,
                UPDATED_CATEGORY_DESCRIPTION);
        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.ofNullable(category));
        Category updateCategory = updateCategoryFromRequest(category, requestDto);
        CategoryResponseDto updatedCategoryDto = categoryToDto(updateCategory);
        when(categoryRepository.save(updateCategory)).thenReturn(updateCategory);
        when(categoryMapper.toDto(updateCategory)).thenReturn(updatedCategoryDto);

        CategoryResponseDto response = categoryService.update(CATEGORY_ID, requestDto);

        assertEquals(updatedCategoryDto, response);
    }

    @DisplayName("Update when category does not exist")
    @Test
    public void update_NotExistingCategory_ThrowsEntityNotFoundException() {
        CategoryRequestDto requestDto = new CategoryRequestDto(UPDATED_CATEGORY_NAME,
                UPDATED_CATEGORY_DESCRIPTION);
        when(categoryRepository.findById(NOT_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());
        String expected = EXPECTED_CATEGORY_NOT_FOUND_MESSAGE + NOT_EXISTING_CATEGORY_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> categoryService.update(NOT_EXISTING_CATEGORY_ID, requestDto));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Delete existing category")
    @Test
    public void delete_ExistingCategory_Ok() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);

        categoryService.deleteById(CATEGORY_ID);

        verify(categoryRepository).existsById(CATEGORY_ID);
    }

    @DisplayName("Delete when category does not exist")
    @Test
    public void delete_NotExistingCategory_ThrowsEntityNotFoundException() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(false);
        String expected = EXPECTED_CATEGORY_NOT_FOUND_MESSAGE + CATEGORY_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> categoryService.deleteById(CATEGORY_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Get all categories with default pageable params")
    @Test
    public void getAll_DefaultPageableParams_ReturnsBookPageByDefaultParams() {
        Category category1 = createCategory();
        Category category2 = createCategory();
        Pageable pageable = PageRequest.of(0, 20);
        List<Category> categories = List.of(category, category1, category2);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 3);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return categoryToDto(category);
        });

        List<CategoryResponseDto> response = categoryService.getAll(pageable);
        List<CategoryResponseDto> categoryDtos = categories.stream()
                .map(this::categoryToDto)
                .toList();

        assertEquals(categoryDtos, response);
    }

    @DisplayName("Get all categories with custom pageable params")
    @Test
    public void getAll_CustomPageableParams_ReturnsCategoryResponseDto() {
        Category category1 = createCategory();
        Category category2 = createCategory();
        Pageable pageable = PageRequest.of(1, 2);
        List<Category> categories = List.of(category2);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return categoryToDto(category);
        });

        List<CategoryResponseDto> response = categoryService.getAll(pageable);
        List<CategoryResponseDto> categoryDtos = categories.stream()
                .map(this::categoryToDto)
                .toList();

        assertEquals(categoryDtos, response);
    }

    @DisplayName("Get all categories when no categories present")
    @Test
    public void getAll_NoCategoriesPresent_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Category> emptyBookPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(categoryRepository.findAll(pageable)).thenReturn(emptyBookPage);

        List<CategoryResponseDto> response = categoryService.getAll(pageable);

        assertEquals(Collections.emptyList(), response);
    }

    @DisplayName("Get category by id when category exists")
    @Test
    public void getById_CategoryExistById_ReturnsCategoryResponseDto() {
        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.ofNullable(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryResponseDto response = categoryService.getById(CATEGORY_ID);

        assertEquals(categoryDto, response);
    }

    @DisplayName("Get category by id when category does not exist")
    @Test
    public void getById_CategoryNotExistById_ReturnsCategoryResponseDto() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        String expected = EXPECTED_CATEGORY_NOT_FOUND_MESSAGE + CATEGORY_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, ()
                -> categoryService.getById(CATEGORY_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Get books by category id with existing id")
    @Test
    public void getBooksByCategoryId_ExcitingId_ReturnsListOfBookDtoWithoutCategoryIds() {
        Book book1 = createBook(TITLE1, ISBN1);
        Book book2 = createBook(TITLE2, ISBN2);
        Book book3 = createBook(TITLE3, ISBN3);
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2, book3);
        Page<Book> bookPage = new PageImpl<>(books, pageable, 3);
        when(bookRepository.findByCategoryId(CATEGORY_ID, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoWithoutCategories(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return toBookDtoWithoutCategories(book);
        });
        List<BookDtoWithoutCategoryIds> expected = books.stream()
                .map(this::toBookDtoWithoutCategories)
                .toList();

        List<BookDtoWithoutCategoryIds> response = categoryService
                .getBooksByCategoryId(CATEGORY_ID, pageable);

        assertEquals(expected, response);
    }

    @DisplayName("Get books by category id with no books present")
    @Test
    public void getBooksByCategoryId_NoBooksWithCategoryId_ReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Book> bookPage = new PageImpl<>(Collections.emptyList(), pageable, 3);
        when(bookRepository.findByCategoryId(CATEGORY_ID, pageable)).thenReturn(bookPage);

        List<BookDtoWithoutCategoryIds> response = categoryService
                .getBooksByCategoryId(CATEGORY_ID, pageable);

        assertEquals(Collections.emptyList(), response);
    }

    private CategoryResponseDto categoryToDto(Category category) {
        return new CategoryResponseDto(category.getId(),
                category.getName(),
                category.getDescription());
    }

    private Category updateCategoryFromRequest(Category category, CategoryRequestDto requestDto) {
        category.setName(requestDto.name());
        category.setDescription(requestDto.description());
        return category;
    }

    private Category createCategory() {
        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setDescription("New Description");
        return newCategory;
    }

    private BookDtoWithoutCategoryIds toBookDtoWithoutCategories(Book book) {
        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds =
                new BookDtoWithoutCategoryIds();
        bookDtoWithoutCategoryIds.setId(book.getId());
        bookDtoWithoutCategoryIds.setAuthor(book.getAuthor());
        bookDtoWithoutCategoryIds.setTitle(book.getTitle());
        bookDtoWithoutCategoryIds.setPrice(book.getPrice());
        bookDtoWithoutCategoryIds.setDescription(book.getDescription());
        bookDtoWithoutCategoryIds.setCoverImage(book.getCoverImage());
        return bookDtoWithoutCategoryIds;
    }

    private Book createBook(String title, String isbn) {
        Book book = new Book();
        book.setAuthor(AUTHOR);
        book.setIsbn(isbn);
        book.setPrice(PRICE);
        book.setTitle(title);
        book.setDescription(CATEGORY_DESCRIPTION);
        book.setCoverImage(COVER_IMAGE);
        book.setCategories(Set.of(category));
        return book;
    }
}
