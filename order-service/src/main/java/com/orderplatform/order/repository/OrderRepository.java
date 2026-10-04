package com.orderplatform.order.repository;

import com.orderplatform.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(Long userId);
}
