package com.nerotomato.kafka.contorller;

import com.nerotomato.kafka.producer.ProducerDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaDemoController {

    @Autowired
    ProducerDemo producer;

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public void sendMessage(@RequestParam(value = "message") String message) {
        producer.send(message);
    }
}
