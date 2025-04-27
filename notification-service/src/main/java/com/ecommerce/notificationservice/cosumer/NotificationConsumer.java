package com.ecommerce.notificationservice.cosumer;


import com.ecommerce.notificationservice.dto.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(topics = "order_topics", groupId = "notification-group")
    public void consumeOrder(Order order) {
        // Simulate sending notification
        System.out.println("Sending notification for order: " + order.getOrderId());
        // Email or SMS logic here
    }
}
