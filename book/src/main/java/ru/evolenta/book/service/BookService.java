package ru.evolenta.book.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.evolenta.book.dto.BookDTO;
import ru.evolenta.book.model.Author;
import ru.evolenta.book.model.Book;
import ru.evolenta.book.repository.AuthorRepository;
import ru.evolenta.book.repository.BookRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Value("${api.key}")
    private String apiKey;

    public Book getBook(int id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book createBook(BookDTO bookDto) {
        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPrice(bookDto.getPrice());
        book.setQuantity(bookDto.getQuantity());

        // Связка книги с авторами
        Set<Author> authors = bookDto.getAuthors().stream()
                .map(authorDTO -> authorRepository.findById(authorDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Author not found: " + authorDTO.getId())))
                .collect(Collectors.toSet());

        book.setAuthors(authors);

        return bookRepository.save(book);
    }

    public Book updateBook(int id, BookDTO bookDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BeanUtils.copyProperties(bookDto, existingBook, "authors");

        Set<Author> authors = bookDto.getAuthors().stream()
                .map(authorDTO -> authorRepository.findById(authorDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Author not found: " + authorDTO.getId())))
                .collect(Collectors.toSet());

        existingBook.setAuthors(authors);

        return bookRepository.save(existingBook);
    }

    public Iterable<Book> upsertBooksList(HttpServletRequest request, List<Book> books) {
        String apiKeyFromHeader = request.getHeader("X-Internal-Api-Key");
        if (apiKeyFromHeader == null || !apiKeyFromHeader.equals(apiKey)) {
            throw new RuntimeException("Missing API Key");
        }
        return bookRepository.saveAll(books);
    }

    public void deleteBook(int id) {
        bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        bookRepository.deleteById(id);
    }
}
