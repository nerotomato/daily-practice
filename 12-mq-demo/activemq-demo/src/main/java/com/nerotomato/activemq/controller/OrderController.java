package com.nerotomato.activemq.controller;

import com.nerotomato.activemq.entity.OrderMessage;
import com.nerotomato.activemq.producer.DemoProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;

@Slf4j
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    DemoProducer producer;

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public void sendMessage(@RequestParam(value = "message") String message) {
        Destination destination = new ActiveMQQueue("testQueue");
        OrderMessage orderMessage = OrderMessage.builder().content(message).type("test").build();
        //发送消息到queue队列
        producer.send(destination, orderMessage);
    }

    @RequestMapping(value = "/delayMessage", method = RequestMethod.POST)
    public void sendDelayMessage(@RequestParam(value = "message") String message) {
        Destination destination = new ActiveMQTopic("cancelOrderTopic");
        // 延时发送消息 单位毫秒
        OrderMessage orderMessage = OrderMessage.builder().content(message).type("order").build();
        producer.delaySend(destination, orderMessage, 5000l);
    }

}
