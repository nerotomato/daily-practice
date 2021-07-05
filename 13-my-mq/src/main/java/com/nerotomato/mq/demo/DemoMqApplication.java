package com.nerotomato.mq.demo;

import com.nerotomato.mq.core.mymq.MyMqBroker;
import com.nerotomato.mq.core.mymq.MyMqConsumer;
import com.nerotomato.mq.core.mymq.MyMqMessage;
import com.nerotomato.mq.core.mymq.MyMqProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoMqApplication {
    @SneakyThrows
    public static void main(String[] args) {
        String topic = "my.test";
        MyMqBroker myMqBroker = new MyMqBroker();
        myMqBroker.createTopic(topic);

        MyMqConsumer consumer = myMqBroker.createConsumer();
        consumer.subscribe(topic);

        final boolean[] flag = new boolean[1];
        flag[0] = true;
        new Thread(() -> {
            while (flag[0]) {
                MyMqMessage message = consumer.poll(100);
                if (null != message) {
                    log.info("======> received message is: {}", message.getBody());
                }
            }
            log.info("==> {}: consumer is exited.", Thread.currentThread().getName());
        }).start();

        MyMqProducer producer = myMqBroker.createProducer();

        for (int i = 0; i < 1000; i++) {
            Order order = new Order(1000l + i, System.currentTimeMillis(), "CNY", 8989898d);
            producer.send(topic, new MyMqMessage(null, order));
        }

        Thread.sleep(500);
        System.out.println("点击任何键，发送一条消息；点击q或e，退出程序。");
        while (true) {
            char c = (char) System.in.read();
            if (c > 20) {
                log.info(c + "");
                producer.send(topic, new MyMqMessage(null, new Order(100000L + c, System.currentTimeMillis(), "CNY", 8989898d)));
            }

            if (c == 'q' || c == 'e') break;
        }

        flag[0] = false;
    }
}
