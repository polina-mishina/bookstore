package ru.evolenta.order.dto;

import lombok.Data;

import java.util.Set;

@Data
public class BookDTO {
    private int id;
    private String title;
    private Set<AuthorDTO> authors;
    private String description;
    private Double price;
    private Integer quantity;
}
