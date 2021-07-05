package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomBroker {

    private Map<String, CustomQueue> queueMap = new ConcurrentHashMap<>();

    /**
     * broker直接发送消息
     */
    public boolean send(String topic, CustomMessage message) {
        CustomQueue queue = queueMap.getOrDefault(topic, new CustomQueue(topic));
        queue.add(message);
        return true;
    }

    /**
     * broker直接消费消息
     */
    public List<CustomMessage> poll(String topic, String group, int num) {
        CustomQueue queue = queueMap.get(topic);
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == queue) {
            return messages;
        }
        log.info("queue message amount : " + queue.size());
        while (!queue.isEmpty() || num > 0) {
            CustomMessage message = queue.get(group);
            if (message == null) {
                break;
            }
            messages.add(message);
            num -= 1;
        }
        return messages;
    }

    /**
     * 根据topic查找队列
     */
    public CustomQueue findCustomQueue(String topic) {
        return this.queueMap.get(topic);
    }

    /**
     * 创建生产者和topic消息队列
     */
    public CustomProducer createProducer(String topic) {
        queueMap.putIfAbsent(topic, new CustomQueue(topic));
        return new CustomProducer(this);
    }

    /**
     * 创建消费者
     */
    public CustomConsumer createConsumer() {
        return new CustomConsumer(this);
    }
}
