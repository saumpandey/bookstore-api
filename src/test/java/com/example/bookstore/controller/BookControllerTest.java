package com.example.bookstore.controller;

import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer slice test: only the controller + JSON + validation, service is mocked. */
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService service;

    @Test
    void list_returnsBooks() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new Book(1L, "Clean Code", "Robert Martin", new BigDecimal("30.00"))));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void get_returns404_whenMissing() throws Exception {
        when(service.findById(42L)).thenThrow(new BookNotFoundException(42L));

        mockMvc.perform(get("/api/books/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book not found with id 42"));
    }

    @Test
    void create_returns201_andBody() throws Exception {
        when(service.create(any(Book.class))).thenReturn(
                new Book(1L, "DDD", "Eric Evans", new BigDecimal("50.00")));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"DDD","author":"Eric Evans","price":50.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("DDD"));
    }

    @Test
    void create_returns400_whenInvalid() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Someone","price":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void delete_returns404_whenMissing() throws Exception {
        doThrow(new BookNotFoundException(7L)).when(service).delete(7L);

        mockMvc.perform(delete("/api/books/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void discountedPrice_returnsComputedValue() throws Exception {
        when(service.discountedPrice(1L, 10)).thenReturn(new BigDecimal("27.00"));

        mockMvc.perform(get("/api/books/1/discounted-price").param("percent", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(27.00));
    }
}
