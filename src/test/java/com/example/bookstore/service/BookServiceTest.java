package com.example.bookstore.service;

import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit test: no Spring context, repository is mocked. */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service;

    private Book book(Long id, String title, String author, String price) {
        return new Book(id, title, author, new BigDecimal(price));
    }

    @Test
    void findAll_returnsAllBooks() {
        when(repository.findAll()).thenReturn(List.of(
                book(1L, "Clean Code", "Robert Martin", "30.00"),
                book(2L, "Refactoring", "Martin Fowler", "40.00")));

        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void findById_returnsBook_whenPresent() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "Clean Code", "Robert Martin", "30.00")));

        assertThat(service.findById(1L).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void findById_throws_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_clearsIdAndSaves() {
        Book input = book(123L, "DDD", "Eric Evans", "50.00");
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book saved = service.create(input);

        assertThat(saved.getId()).isNull();
        verify(repository).save(input);
    }

    @Test
    void update_changesFields() {
        Book existing = book(1L, "Old", "Someone", "10.00");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = service.update(1L, book(null, "New", "Other", "20.00"));

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getAuthor()).isEqualTo("Other");
        assertThat(result.getPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void delete_throws_whenMissing_andDoesNotDelete() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(BookNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void discountedPrice_applies10PercentDiscount() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "Clean Code", "Robert Martin", "30.00")));

        assertThat(service.discountedPrice(1L, 10)).isEqualByComparingTo("27.00");
    }

    @Test
    void discountedPrice_roundsToTwoDecimals() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "Odd Price", "A", "9.99")));

        assertThat(service.discountedPrice(1L, 33)).isEqualByComparingTo("6.69");
    }

    @Test
    void discountedPrice_rejectsInvalidPercent() {
        assertThatThrownBy(() -> service.discountedPrice(1L, 101))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.discountedPrice(1L, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
