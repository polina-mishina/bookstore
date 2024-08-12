package ru.evolenta.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"order\"")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {
    @Id @GeneratedValue
    private int id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}
