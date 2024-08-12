package ru.evolenta.order.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.evolenta.order.model.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, Integer> {
    Iterable<Order> findAllByUserId(long userId);
}
