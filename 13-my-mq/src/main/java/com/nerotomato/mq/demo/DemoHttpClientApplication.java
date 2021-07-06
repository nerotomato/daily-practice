package com.nerotomato.mq.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerotomato.mq.api.HttpConsumer;
import com.nerotomato.mq.api.HttpProducer;
import com.nerotomato.mq.core.custom.CustomMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过httpclient 发生消息和消费消息
 */
@Slf4j
public class DemoHttpClientApplication {

    public static void main(String[] args) {
        String topic = "springmvc.topic";
        String url = "http://localhost:8080";
        //生产者发生消息
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put("url", url);

        HttpProducer httpProducer = new HttpProducer(producerProps);
        for (int i = 0; i < 1000; i++) {
            httpProducer.send(topic, "test mq based on springmvc " + i);
        }
        //消费者消费消息
        new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                Map<String, Object> consumerProps = new HashMap<>();
                consumerProps.put("topic", topic);
                consumerProps.put("url", url);
                consumerProps.put("group", Thread.currentThread().getId() + "-" + Thread.currentThread().getName());
                HttpConsumer httpConsumer = new HttpConsumer(consumerProps);
                List<LinkedHashMap> list = httpConsumer.poll(1000);
                for (LinkedHashMap map : list) {
                    log.info("======> received message is {}", map.get("body").toString());
                }
            }
        }.start();

    }
}
