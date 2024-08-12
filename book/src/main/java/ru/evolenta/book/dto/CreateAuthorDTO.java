package ru.evolenta.book.dto;

import lombok.Data;

@Data
public class CreateAuthorDTO {
    private String name;
    private String surname;
    private String patronymic;
}
