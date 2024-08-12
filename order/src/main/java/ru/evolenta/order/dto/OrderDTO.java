package ru.evolenta.order.dto;

import lombok.Data;
import ru.evolenta.order.model.Status;

import java.util.List;

@Data
public class OrderDTO {
    private int id;
    private long userId;
    private Status status;
    private List<OrderItemDTO> orderItems;
    private Double totalPrice; // Общая стоимость заказа
}
