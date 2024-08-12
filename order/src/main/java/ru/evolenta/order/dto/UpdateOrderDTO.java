package ru.evolenta.order.dto;

import lombok.Data;
import ru.evolenta.order.model.OrderItem;
import ru.evolenta.order.model.Status;

import java.util.List;

@Data
public class UpdateOrderDTO {
    private List<OrderItem> orderItems;
}
