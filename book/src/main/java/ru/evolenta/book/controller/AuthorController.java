package ru.evolenta.book.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.evolenta.book.dto.CreateAuthorDTO;
import ru.evolenta.book.model.Author;
import ru.evolenta.book.repository.AuthorRepository;
import ru.evolenta.book.service.AuthorService;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public Iterable<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthor(@PathVariable int id) {
        try {
            Author author = authorService.getAuthor(id);
            return ResponseEntity.ok(author);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Author createAuthor(@RequestBody CreateAuthorDTO authorDto) {
        return authorService.createAuthor(authorDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable int id, @RequestBody CreateAuthorDTO authorDto) {
        try {
            Author author = authorService.updateAuthor(id, authorDto);
            return ResponseEntity.ok(author);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
