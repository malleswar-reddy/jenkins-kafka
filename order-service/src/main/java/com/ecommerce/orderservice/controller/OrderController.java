package com.ecommerce.orderservice.controller;

import com.ecommerce.commonservice.Order;
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