package com.orderplatform.order.controller;

import com.orderplatform.order.client.InventoryClient;
import com.orderplatform.order.document.Order;
import com.orderplatform.order.dto.OrderEvent;
import com.orderplatform.order.dto.OrderRequest;
import com.orderplatform.order.kafka.OrderEventProducer;
import com.orderplatform.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderEventProducer orderEventProducer;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        // Step 1: try to reserve stock in inventory-service (synchronous call via Feign + Eureka).
        try {
            inventoryClient.reserveStock(request.getProductId(), Map.of("quantity", request.getQuantity()));
        } catch (HttpClientErrorException.Conflict ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Insufficient stock"));
        }

        // Step 2: persist the order in MongoDB as PLACED.
        Order order = new Order(null, request.getUserId(), request.getProductId(),
                request.getQuantity(), null, "PLACED", Instant.now());
        Order saved = orderRepository.save(order);

        // Step 3: publish an event to Kafka. notification-service consumes this asynchronously.
        orderEventProducer.publish(new OrderEvent(saved.getId(), saved.getUserId(),
                saved.getProductId(), saved.getQuantity(), saved.getStatus()));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersForUser(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @GetMapping
    public List<Order> getAll() {
        return orderRepository.findAll();
    }
}
