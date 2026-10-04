package com.orderplatform.notification.kafka;

import com.orderplatform.notification.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class OrderEventConsumer {

    // In-memory store just for this demo, so you can GET /api/notifications and see
    // what's been processed. A real system would send an email/SMS/push here instead.
    private final List<String> notificationLog = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvent(OrderEvent event) {
        String message = String.format("[%s] Order %s placed by user %d for product %d (qty %d) - status: %s",
                Instant.now(), event.getOrderId(), event.getUserId(), event.getProductId(),
                event.getQuantity(), event.getStatus());
        log.info(message);
        notificationLog.add(message);
    }

    public List<String> getNotificationLog() {
        return Collections.unmodifiableList(notificationLog);
    }
}
