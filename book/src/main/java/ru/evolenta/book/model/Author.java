package ru.evolenta.book.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Author {

    @Id @GeneratedValue
    private int id;
    private String name;
    private String surname;
    private String patronymic;

    @ManyToMany(mappedBy = "authors") @JsonIgnore
    private Set<Book> books;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Author))
            return false;
        return id == (((Author) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
