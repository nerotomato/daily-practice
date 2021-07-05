package com.nerotomato.mq.core.mymq;

public class MyMqProducer {

    private MyMqBroker myMqBroker;

    public MyMqProducer(MyMqBroker myMqBroker) {
        this.myMqBroker = myMqBroker;
    }

    public boolean send(String topic, MyMqMessage message) {
        MyMq myMq = myMqBroker.findMyMq(topic);
        if (null == myMq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
        return myMq.send(message);
    }
}
