package com.ecommerce.notificationservice.controller;

import com.ecommerce.commonservice.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class NotificationController {

    private final Map<String,Order> messages = new ConcurrentHashMap<>();

    @KafkaListener(topics = "order_topics", groupId = "notification-group")
    public void consume(Order record) {
        log.info("Consumed message: {}", record);
        messages.put(record.getOrderId(),record);
    }

    @GetMapping("/notifications")
    public Map<String,Order> getAllMessages() {
        return messages;
    }

    @GetMapping("/notifications/recent")
    public String getRecentMessage() {
        return messages.isEmpty() ? "No messages available" : "Messages available : "+messages.size();
    }
}