package com.nerotomato.mq.core.mymq;

public class MyMqConsumer {

    private MyMqBroker myMqBroker;
    private MyMq myMq;

    public MyMqConsumer(MyMqBroker myMqBroker) {
        this.myMqBroker = myMqBroker;
    }

    public void subscribe(String topic) {
        myMq = myMqBroker.findMyMq(topic);
        if (null == myMq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
    }

    public MyMqMessage poll(long timeout) {
        return myMq.poll(timeout);
    }
}
