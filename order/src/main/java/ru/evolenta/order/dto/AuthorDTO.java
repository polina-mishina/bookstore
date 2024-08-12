package ru.evolenta.order.dto;

import lombok.Data;

@Data
public class AuthorDTO {
    private int id;
    private String name;
    private String surname;
    private String patronymic;
}
