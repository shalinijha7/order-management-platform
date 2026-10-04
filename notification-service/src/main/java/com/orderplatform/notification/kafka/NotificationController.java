package com.orderplatform.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final OrderEventConsumer orderEventConsumer;

    @GetMapping
    public List<String> getAll() {
        return orderEventConsumer.getNotificationLog();
    }
}
