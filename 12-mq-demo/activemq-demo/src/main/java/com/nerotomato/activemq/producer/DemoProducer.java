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
