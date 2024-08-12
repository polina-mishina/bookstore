package ru.evolenta.order.dto;

import lombok.Data;
import ru.evolenta.order.model.OrderItem;

import java.util.List;

@Data
public class CreateOrderDTO {
    private List<OrderItem> orderItems;
}
