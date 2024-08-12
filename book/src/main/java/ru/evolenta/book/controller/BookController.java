package ru.evolenta.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.evolenta.book.dto.BookDTO;
import ru.evolenta.book.model.Book;
import ru.evolenta.book.repository.BookRepository;
import ru.evolenta.book.service.BookService;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @GetMapping
    public Iterable<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable int id) {
        try {
            Book findedBook = bookService.getBook(id);
            return ResponseEntity.ok(findedBook);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody BookDTO bookDto) {
        try {
            Book createdBook = bookService.createBook(bookDto);
            return ResponseEntity.ok(createdBook);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable int id, @RequestBody BookDTO bookDto) {
        try {
            Book updatedBook = bookService.updateBook(id, bookDto);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public Iterable<Book> upsertBooksList(
            HttpServletRequest request,
            @RequestBody List<Book> books
    ) {
        try {
            return bookService.upsertBooksList(request, books);
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable int id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
