package com.nerotomato.activemq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication
@EnableJms //启动消息队列
public class ActiveMQDemoApplication implements ApplicationRunner {
    @Value("${spring.activemq.broker-url}")
    private String BROKER_URL;

    public static void main(String[] args) {
        SpringApplication.run(ActiveMQDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //测试queue模式和topic模式的队列的消息的发送和接收
        testBasicQueueAndTopic();
    }

    /**
     *测试queue模式和topic模式的队列的消息的发送和接收
     */
    private void testBasicQueueAndTopic() {
        // 定义topic Destination
        ActiveMQDestination topicDestination = new ActiveMQTopic("testTopic");
        // 定义queue Destination
        ActiveMQDestination queueDestination = new ActiveMQQueue("testQueue");
        //testDestination(topicDestination);
        //testDestination(queueDestination);
        /**
         * Thread.sleep方法，会让调用到该方法的线程处理waitting状态
         * 释放CPU，不释放锁或监视器
         * 但是如果主线程调用到Thread.sleep这里了，主线程也会处于等待状态，主线程后面的代码不一定能执行
         * 上面测试时主线程执行testDestination(topicDestination);
         * 主线程后面new的线程new Thread(() -> testDestination(queueDestination)).start();没有执行
         * 要等主线程调用的Thread.sleep方法设置的时间结束才会执行
         * */
        //开启线程执行topic队列消息发送
        new Thread(() -> testDestination(topicDestination)).start();
        //开启线程执行queue队列消息发送
        new Thread(() -> testDestination(queueDestination)).start();
    }

    private void testDestination(ActiveMQDestination destination) {

        try {
            // 创建连接
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
            connection.start();
            //创建会话
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 创建消费者
            MessageConsumer consumer = session.createConsumer(destination);
            AtomicInteger counter = new AtomicInteger(0);

            MessageListener messageListener = new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    log.info(counter.incrementAndGet() + "==>" + "receive from" + destination.toString() + ": " + message);
                }
            };
            // 绑定消息监听器
            consumer.setMessageListener(messageListener);

            // 创建生产者，生产100个消息
            MessageProducer producer = session.createProducer(destination);
            int index = 0;
            while (index++ < 100) {
                String message = "";
                if (destination.getDestinationType() == 2) {
                    message = "Hello topic mq!!";
                } else if (destination.getDestinationType() == 1) {
                    message = "Hello queue mq!!";
                }
                TextMessage textMessage = session.createTextMessage(message);
                producer.send(textMessage);
            }

            /**
             * Thread.sleep方法，会让调用该方法的线程处理waitting状态
             * 释放CPU，不释放锁或监视器
             * 但是如果主线程调用到这里了，主线程处于等待状态，主线程后面的代码不一定能执行，后面new的线程没有执行
             * */
            Thread.sleep(5000);
            log.info(Thread.currentThread().getName() + " : 线程sleep完毕！");
            session.close();
            connection.close();
        } catch (JMSException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
