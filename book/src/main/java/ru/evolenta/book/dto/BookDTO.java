package ru.evolenta.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookDTO {
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
    private Set<AuthorDTO> authors;
}
