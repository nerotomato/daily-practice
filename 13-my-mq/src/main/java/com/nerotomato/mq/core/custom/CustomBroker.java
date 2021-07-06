package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomBroker {

    /**
     * 消息队列集合
     * key - topic
     * value - CustomQueue
     */
    private Map<String, CustomQueue> queueMap = new ConcurrentHashMap<>();

    /**
     * 拆分producer和consumer
     * 由broker发送消息
     */
    public boolean send(String topic, CustomMessage message) {
        CustomQueue queue = queueMap.get(topic);
        if (null == queue) {
            queue = new CustomQueue(topic);
            queueMap.put(topic, queue);
        }
        queue.add(message);
        return true;
    }

    /**
     * 拆分producer和consumer
     * 从broker消费消息
     * num > 0 表示指定消费 num 数量的消息
     * num=-1 表示消费队列剩余全部消息
     */
    public List<CustomMessage> poll(String topic, String group, int num) {
        CustomQueue queue = queueMap.get(topic);
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == queue) {
            return messages;
        }
        log.info("queue message amount : " + queue.size());
        if (num == -1) {
            while (!queue.isEmpty()) {
                CustomMessage message = queue.get(group);
                if (message == null) {
                    break;
                }
                messages.add(message);
            }
            return messages;
        }
        while (!queue.isEmpty() && num > 0) {
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

    /**
     * 创建消息队列
     */
    public Object createTopic(String topic) {
        CustomQueue queue = findCustomQueue(topic);
        if (null == queue) {
            queue = new CustomQueue(topic);
            queueMap.put(topic, queue);
            return "The topic [ " + topic + " ] has benn created successfully!";
        }
        return "The topic [ " + topic + " ] is exist.";
    }
}
