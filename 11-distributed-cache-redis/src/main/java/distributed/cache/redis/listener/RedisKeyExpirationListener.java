package distributed.cache.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 监听所有db的过期事件__keyevent@*__:expired"
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final String ORDER_KEY_PREFIX = "Order_";

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        //获取失效的key
        String expireKey = message.toString();
        log.info("========== redis key: {} expired ==========", expireKey);
        if (expireKey != null && expireKey.startsWith(ORDER_KEY_PREFIX)) {
            //根据过期的key，获取超时未支付订单号，publish消息到redis consumer异步处理
            //TODO
            String orderId = expireKey.split(ORDER_KEY_PREFIX)[1];
            //redis发送（publish）取消订单消息到channel通道 - CancelOrderChannel
            //message - orderIdStr
            redisTemplate.convertAndSend("CancelOrderChannel", orderId);
            log.info("========== Send order cancel message to CancelOrderChannel. OrderId: {}", orderId);
        }
    }
}
