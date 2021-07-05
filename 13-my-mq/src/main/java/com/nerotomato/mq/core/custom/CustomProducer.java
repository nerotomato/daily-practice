package com.nerotomato.mq.core.custom;

public class CustomProducer {

    private CustomBroker customBroker;

    public CustomProducer(CustomBroker customBroker) {
        this.customBroker = customBroker;
    }

    public boolean send(String topic, CustomMessage message) {
        CustomQueue customQueue = customBroker.findCustomQueue(topic);
        if (null == customQueue) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
        return customQueue.add(message);
    }

}
