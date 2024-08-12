package ru.evolenta.book.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.evolenta.book.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends CrudRepository<Book, Integer> {
}
