package ru.evolenta.order.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.evolenta.order.model.OrderItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItem, Integer> {
    List<OrderItem> findAllByOrderId(int orderId);
}
