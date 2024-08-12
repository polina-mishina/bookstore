package ru.evolenta.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderItem {
    @Id @GeneratedValue
    private int id;

    @ManyToOne @JsonIgnore
    @JoinColumn(name = "order_id")
    private Order order;

    private int bookId;

    private Integer quantity;
}


