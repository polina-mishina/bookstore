package ru.evolenta.book.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evolenta.book.dto.CreateAuthorDTO;
import ru.evolenta.book.model.Author;
import ru.evolenta.book.repository.AuthorRepository;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    public Author createAuthor(CreateAuthorDTO authorDto) {
        Author author = new Author();
        BeanUtils.copyProperties(authorDto, author);
        return authorRepository.save(author);
    }

    public Author getAuthor(int id) {
        return authorRepository.findById(id).orElseThrow(() -> new RuntimeException("Author not found"));
    }

    public Author updateAuthor(int id, CreateAuthorDTO authorDto) {
        Author existingAuthor = authorRepository.findById(id).orElseThrow(() -> new RuntimeException("Author not found"));
        BeanUtils.copyProperties(authorDto, existingAuthor);
        return authorRepository.save(existingAuthor);
    }

    public void deleteAuthor(int id) {
        authorRepository.findById(id).orElseThrow(() -> new RuntimeException("Author not found"));
        authorRepository.deleteById(id);
    }
}
