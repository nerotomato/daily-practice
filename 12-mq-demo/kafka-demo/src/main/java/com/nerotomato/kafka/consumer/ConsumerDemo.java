package com.nerotomato.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * 简单消费者
 * */
@Slf4j
@Component
public class ConsumerDemo {

    // 消费监听
    @KafkaListener(topics = {"testTopic"})
    public void onMessage(ConsumerRecord<?, ?> record) {
        // 消费的哪个topic、partition的消息,打印出消息内容
        log.info("======> Message received : " + "Topic: " + record.topic() + ",Partition: " + record.partition() + ",Value: " + record.value());
    }
}
