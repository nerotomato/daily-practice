package com.nerotomato.activemq.consumer;

import com.nerotomato.activemq.entity.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoConsumer {
    // 使用JmsListener配置消费者监听的队列
    //测试延时队列，模拟订单超时取消订单
    @JmsListener(destination = "cancelOrderTopic", containerFactory = "topicJmsListenerContainerFactory")
    public void receivedTopic(OrderMessage message) {
        log.info("======> Received delay message is: {}", message.toString());
    }
}
