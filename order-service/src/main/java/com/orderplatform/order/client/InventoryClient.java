package com.orderplatform.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

// "inventory-service" here is resolved via Eureka service discovery, not a hardcoded host:port.
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/products/{id}/reserve")
    ResponseEntity<Map<String, Object>> reserveStock(@PathVariable("id") Long productId,
                                                       @RequestBody Map<String, Integer> body);
}
