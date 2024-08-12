package ru.evolenta.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.evolenta.order.dto.CreateOrderDTO;
import ru.evolenta.order.dto.OrderDTO;
import ru.evolenta.order.dto.UpdateOrderDTO;
import ru.evolenta.order.dto.UpdateOrderStatusDTO;
import ru.evolenta.order.model.Order;
import ru.evolenta.order.service.OrderService;

import java.util.Collections;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Iterable<OrderDTO> getAllOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            return orderService.getAllOrders(authHeader);
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    @GetMapping("/user/{id}")
    public Iterable<OrderDTO> getAllOrdersByUserId(
            @PathVariable long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return orderService.getAllOrdersByUserId(id, authHeader);
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    @GetMapping("/me")
    public Iterable<OrderDTO> getAllOrdersCurrentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            return orderService.getAllOrdersCurrentUser(authHeader);
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody CreateOrderDTO createOrderDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            Order createdOrder = orderService.createOrder(createOrderDTO, authHeader);
            return ResponseEntity.ok(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable int id,
            @RequestBody UpdateOrderDTO updateOrderDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            Order updatedOrder = orderService.updateOrder(id, updateOrderDTO, authHeader);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatusOrder(@PathVariable int id, @RequestBody UpdateOrderStatusDTO status) {
        try {
            Order updatedOrder = orderService.updateStatusOrder(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable int id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            orderService.deleteOrder(id, authHeader);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
