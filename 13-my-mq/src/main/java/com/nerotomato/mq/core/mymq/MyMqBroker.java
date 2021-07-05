package com.nerotomato.mq.core.mymq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MyMqBroker {
    public static final int CAPACITY = 10000;

    private final Map<String, MyMq> myMqMap = new ConcurrentHashMap<>(64);

    public void createTopic(String name) {
        myMqMap.putIfAbsent(name, new MyMq(name, CAPACITY));
    }

    public MyMq findMyMq(String topic) {
        return this.myMqMap.get(topic);
    }

    public MyMqProducer createProducer() {
        return new MyMqProducer(this);
    }

    public MyMqConsumer createConsumer() {
        return new MyMqConsumer(this);
    }
}
