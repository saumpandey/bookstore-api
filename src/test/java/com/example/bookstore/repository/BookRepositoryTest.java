package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** JPA slice test against an in-memory H2 database. */
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    @Test
    void findByAuthorIgnoreCase_matchesRegardlessOfCase() {
        repository.save(new Book(null, "Clean Code", "Robert Martin", new BigDecimal("30.00")));
        repository.save(new Book(null, "Clean Architecture", "Robert Martin", new BigDecimal("35.00")));
        repository.save(new Book(null, "Refactoring", "Martin Fowler", new BigDecimal("40.00")));

        List<Book> result = repository.findByAuthorIgnoreCase("robert martin");

        assertThat(result).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
    }

    @Test
    void findByAuthorIgnoreCase_returnsEmpty_whenNoMatch() {
        assertThat(repository.findByAuthorIgnoreCase("Nobody")).isEmpty();
    }
}
