package ru.evolenta.book.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.evolenta.book.model.Author;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
}
