package ru.evolenta.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemDTO {
    private int bookId;
    private String title;
    private Integer quantity;
    private Double price;
    private Double totalPrice; // Общая стоимость книг в заказе
}
