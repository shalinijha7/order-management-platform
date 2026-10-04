package com.orderplatform.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the payload published to Kafka topic "order-events".
// notification-service consumes the same shape of object.
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
