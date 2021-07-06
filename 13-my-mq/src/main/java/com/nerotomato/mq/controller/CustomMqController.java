package com.nerotomato.mq.controller;

import com.nerotomato.mq.core.custom.CustomBroker;
import com.nerotomato.mq.core.custom.CustomMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/custom")
public class CustomMqController {

    @Autowired
    CustomBroker customBroker;

    @RequestMapping(value = "/createTopic", method = RequestMethod.POST)
    public Object create(@RequestParam(value = "topic") String topic) {
        return customBroker.createTopic(topic);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public Object send(@RequestBody CustomMessage message) {

        log.info("======> Start sending message to {}", message.getTopic());
        long startTime = System.currentTimeMillis();
        boolean send = customBroker.send(message.getTopic(), message);
        long endTime = System.currentTimeMillis();
        log.info("The time cost of producer is {}", endTime - startTime);
        return send;
    }

    @RequestMapping(value = "/poll", method = RequestMethod.GET)
    public Object poll(@RequestParam(value = "group") String group,
                       @RequestParam(value = "topic") String topic,
                       @RequestParam(value = "num") int num) {
        List<CustomMessage> customMessages = null;
        while (true) {
            customMessages = customBroker.poll(topic, group, num);
            break;
        }
        return customMessages;
    }

}
