package com.ecommerce.stockservice.service;


import com.ecommerce.commonservice.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StockConsumer {

    @KafkaListener(topics = "order_topics", groupId = "stock-group")
    public void consumeOrder(Order order) {
        // Simulate inventory update
//        System.out.println("Processing order: " + order.getOrderId() + ", Product: " + order.getProductId() + ", Quantity: " + order.getQuantity());
        // Update inventory logic here
    }
}