[TOC]



# 1.基于queue点对点模式和topic发布订阅模式的消息队列练习

```java
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

```



# 2.练习activemq延时队列消息

```java
package com.nerotomato.activemq.config;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ActiveMqConfig {

  /*  @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(@Value("${spring.activemq.broker-url}") String brokerUrl) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        // 设置信任序列化包集合
        List<String> packages = new ArrayList<>();
        packages.add("com.nerotomato.activemq.entity");
        activeMQConnectionFactory.setTrustedPackages(packages);
        return activeMQConnectionFactory;
    }*/

    //这个Bean用于和mq建立连接、mq的地址、用户名、密码从配置文件中获取
    @Bean(name = "innerConnectionFactory")
    @Primary
    public ActiveMQConnectionFactory firstConnectionFactory(
            //mq服务地址
            @Value("${spring.activemq.broker-url}") String brokerUrl,
            //mq用户名
            @Value("${spring.activemq.user}") String username,
            //mq密码
            @Value("${spring.activemq.password}") String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);
        factory.setTrustAllPackages(true);
        factory.setMaxThreadPoolSize(ActiveMQConnection.DEFAULT_THREAD_POOL_SIZE);
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        //定义ReDelivery(重发机制)机制 ，重发时间间隔是100毫秒，最大重发次数是3次
        //是否在每次尝试重新发送失败后,增长这个等待时间
        redeliveryPolicy.setUseExponentialBackOff(true);
        //重发次数,默认为6次   这里设置为1次
        redeliveryPolicy.setMaximumRedeliveries(1);
        //重发时间间隔,默认为1秒
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        //第一次失败后重新发送之前等待500毫秒,第二次失败再等待500 * 2毫秒,这里的2就是value
        redeliveryPolicy.setBackOffMultiplier(2);
        //最大传送延迟，只在useExponentialBackOff为true时有效（V5.5），假设首次重连间隔为10ms，倍数为2，那么第
        //二次重连时间间隔为 20ms，第三次重连时间间隔为40ms，当重连时间间隔大的最大重连时间间隔时，以后每次重连时间间隔都为最大重连时间间隔。
        redeliveryPolicy.setMaximumRedeliveryDelay(1000);
        factory.setRedeliveryPolicy(redeliveryPolicy);
        return factory;
    }

    //在Topic模式中，对消息的监听需要对containerFactory进行配置
    @Bean("topicJmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
            //将“innerConnectionFactory”这个Bean注入
            @Qualifier("innerConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        //设置pub/sub模式
        factory.setPubSubDomain(true);
        return factory;
    }

    // 在Queue模式中，对消息的监听需要对containerFactory进行配置
    @Bean("queueJmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(
            //将“innerConnectionFactory”这个Bean注入
            @Qualifier("innerConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        factory.setPubSubDomain(false);
        return factory;
    }

}

```



```java
package com.nerotomato.activemq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;

@Slf4j
@Component
public class DemoProducer {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 发送消息
     *
     * @param destination destination是发送到的队列
     * @param message     message是待发送的消息
     */
    public <T> void send(Destination destination, T message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }

    /**
     * 延时发送
     * 测试延时队列，模拟订单超时取消订单
     * @param destination 发送的队列
     * @param message     发送的消息
     * @param time        延迟时间
     */
    public <T extends Serializable> void delaySend(Destination destination, T message, Long time) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            //获取连接工厂
            ConnectionFactory connectionFactory = jmsMessagingTemplate.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.start();
            //获取session，true开启事务，false关闭事务
            session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            //创建生产者和消息队列
            producer = session.createProducer(destination);
            producer.setDeliveryMode(JmsProperties.DeliveryMode.PERSISTENT.getValue());
            ObjectMessage objectMessage = session.createObjectMessage(message);
            //设置延迟时间
            objectMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, time);
            producer.send(objectMessage);
            log.info("发送消息：{}", message);
            //commit transaction
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            try {
                if (producer != null) {
                    producer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

```

```java
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

```

