package com.orderplatform.order.kafka;

import com.orderplatform.order.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(OrderEvent event) {
        // Key by orderId so events for the same order land on the same partition, in order.
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }
}
