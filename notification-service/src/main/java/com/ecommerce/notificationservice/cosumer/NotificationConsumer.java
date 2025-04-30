package com.ecommerce.notificationservice.cosumer;


import com.ecommerce.commonservice.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "order_topics", groupId = "notification-group")
    public void consume(Order order) {
        if (!isValidProductId(order.getProductId())) {
            log.error("Invalid productId: {}", order.getProductId());
            throw new IllegalArgumentException("Invalid productId");
        }
        log.info("Sending notification for order: {}", order.getOrderId());
        // Notification logic
    }

    private boolean isValidProductId(String productId) {
        return productId != null && productId.matches("^PROD\\d+$");
    }
}
