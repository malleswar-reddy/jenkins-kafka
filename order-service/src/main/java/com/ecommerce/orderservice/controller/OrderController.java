package com.ecommerce.orderservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    @PostMapping
    public String placeOrder(@RequestBody Order order) {
        kafkaTemplate.send("order_topics", order.getOrderId(), order);
        return "Order placed successfully: " + order.getOrderId();
    }
}

class Order {
    private String orderId;
    private String productId;
    private int quantity;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}