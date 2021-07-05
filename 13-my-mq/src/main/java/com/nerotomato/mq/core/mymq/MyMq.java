package com.nerotomato.mq.core.mymq;

import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyMq {
    private String topic;
    private int capacity;
    private LinkedBlockingQueue<MyMqMessage> queue;

    public MyMq(String topic, int capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public boolean send(MyMqMessage myMqMessage) {
        return queue.offer(myMqMessage);
    }

    public MyMqMessage poll(){
        return queue.poll();
    }

    @SneakyThrows
    public MyMqMessage poll(long timeout) {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }
}
