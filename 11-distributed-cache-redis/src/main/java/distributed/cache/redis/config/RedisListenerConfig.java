package distributed.cache.redis.config;

import distributed.cache.redis.consumer.RedisConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * redis缓存过期通知配置类 - 底层基于pub/sub
 * redis消息订阅监听配置类 - 配置sub端监听通道和方法
 */
@Configuration
public class RedisListenerConfig {

    /**
     * @param redisConnectionFactory
     * @param messageListenerAdapter
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                            MessageListenerAdapter messageListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        //设置sub端监听通道 - 取消订单通道 CancelOrderChannel
        container.addMessageListener(messageListenerAdapter, new PatternTopic("CancelOrderChannel"));
        return container;
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter(RedisConsumer redisConsumer) {
        /**
         * 方法名与RedisConsumer中用于回调的方法名保持一致
         */
        return new MessageListenerAdapter(redisConsumer, "receiveMessage");
    }

}


