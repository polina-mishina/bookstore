package ru.evolenta.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateAuthorDTO {
    private String name;
    private String surname;
    private String patronymic;
}
