package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CustomConsumer {
    /**
     * 消费者所属组
     */
    private String group;
    private CustomBroker customBroker;
    private CustomQueue customQueue;

    public CustomConsumer(CustomBroker customBroker) {
        this.group = Thread.currentThread().getId() + " - " + Thread.currentThread().getName();
        this.customBroker = customBroker;
    }

    /**
     * 消费者订阅topic
     */
    public void subscribe(String topic) {
        customQueue = customBroker.findCustomQueue(topic);
        if (null == customQueue) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
    }

    /**
     * 消费者 消费消息
     * 可指定消费-num数量的消息
     */
    public List<CustomMessage> poll(String topic, int num) {
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == customQueue) {
            return messages;
        }
        log.info("queue message amount : " + customQueue.size());
        while (!customQueue.isEmpty() || num > 0) {
            CustomMessage message = customQueue.get(group);
            if (message == null) {
                break;
            }
            messages.add(message);
            num -= 1;
        }
        return messages;
    }
}
