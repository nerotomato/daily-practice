package com.nerotomato.mq.demo;

import com.nerotomato.mq.core.custom.CustomBroker;
import com.nerotomato.mq.core.custom.CustomConsumer;
import com.nerotomato.mq.core.custom.CustomMessage;
import com.nerotomato.mq.core.custom.CustomProducer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DemoCustomApplication {
    public static void main(String[] args) {
        String topic = "custom.topic";
        CustomBroker customBroker = new CustomBroker();
        CustomProducer producer = customBroker.createProducer(topic);

        log.info("======> Start sending message to {}", topic);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            CustomMessage<String> message = new CustomMessage<>(null, topic, "test Custom Message " + i);
            producer.send(topic, message);
        }
        long endTime = System.currentTimeMillis();
        log.info("The time cost of producer is {}", endTime - startTime);

        //消费者线程
        //
        new Thread() {
            @Override
            public void run() {
                CustomConsumer consumer = customBroker.createConsumer();
                consumer.subscribe(topic);
                while (true) {
                    long startTime = System.currentTimeMillis();
                    List<CustomMessage> customMessages = consumer.poll(topic, 0);
                    for (CustomMessage message : customMessages) {
                        log.info(message.getBody().toString());
                    }
                    long endTime = System.currentTimeMillis();
                    log.info("The time cost of Consumer is {} milliseconds", endTime - startTime);
                    break;
                }
            }
        }.start();
    }
}
