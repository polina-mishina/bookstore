package ru.evolenta.order.dto;

import lombok.Data;
import ru.evolenta.order.model.Status;


@Data
public class UpdateOrderStatusDTO {
    private Status status;
}
