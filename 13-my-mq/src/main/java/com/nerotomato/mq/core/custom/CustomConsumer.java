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
     * num > 0 表示指定消费 num 数量的消息
     * num=-1 表示消费队列剩余全部消息
     */
    public List<CustomMessage> poll(String topic, int num) {
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == customQueue) {
            return messages;
        }
        log.info("queue message amount : " + customQueue.size());
        if(num == -1){
            while (!customQueue.isEmpty()) {
                CustomMessage message = customQueue.get(group);
                if (message == null) {
                    break;
                }
                messages.add(message);
            }
            return messages;
        }
        while (!customQueue.isEmpty() && num > 0) {
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
