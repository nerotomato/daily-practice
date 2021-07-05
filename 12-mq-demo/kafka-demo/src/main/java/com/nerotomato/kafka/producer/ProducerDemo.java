package com.nerotomato.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 简单生产者
 */
@Component
public class ProducerDemo {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String message) {
        kafkaTemplate.send("testTopic", message);
    }
}
