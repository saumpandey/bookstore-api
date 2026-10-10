package com.example.bookstore;

import com.example.bookstore.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test (*IT): boots the full app on a random port with H2,
 * and exercises the real HTTP API end to end. Runs in `mvn verify` via Failsafe.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthEndpoint_isUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void createReadUpdateDelete_fullLifecycle() {
        // create
        Book toCreate = new Book(null, "Clean Code", "Robert Martin", new BigDecimal("30.00"));
        ResponseEntity<Book> created = rest.postForEntity("/api/books", toCreate, Book.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().getId();
        assertThat(id).isNotNull();

        // read
        Book fetched = rest.getForObject("/api/books/" + id, Book.class);
        assertThat(fetched.getTitle()).isEqualTo("Clean Code");

        // discounted price
        BigDecimal discounted = rest.getForObject("/api/books/" + id + "/discounted-price?percent=50", BigDecimal.class);
        assertThat(discounted).isEqualByComparingTo("15.00");

        // update
        fetched.setTitle("Clean Code (2nd Ed.)");
        rest.put("/api/books/" + id, fetched);
        assertThat(rest.getForObject("/api/books/" + id, Book.class).getTitle())
                .isEqualTo("Clean Code (2nd Ed.)");

        // delete
        rest.delete("/api/books/" + id);
        assertThat(rest.getForEntity("/api/books/" + id, String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_withInvalidPayload_returns400() {
        Book invalid = new Book(null, "", "", new BigDecimal("-1"));

        ResponseEntity<String> response = rest.postForEntity("/api/books", invalid, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
