package ru.evolenta.book.dto;

import lombok.Data;

import java.util.Set;


@Data
public class BookDTO {
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
    private Set<AuthorDTO> authors;
}
