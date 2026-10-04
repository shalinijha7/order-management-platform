package com.orderplatform.order.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    private Long userId;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
    private String status; // PLACED, FAILED
    private Instant createdAt;
}
