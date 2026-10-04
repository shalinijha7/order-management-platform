package com.orderplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mirrors order-service's OrderEvent shape. In a real system this would live
// in a shared library/schema registry (e.g. Avro + Confluent Schema Registry)
// instead of being duplicated across services.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String status;
}
