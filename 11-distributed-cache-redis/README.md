[TOC]



# 1.redis实现分布式锁

## 1.1.springboot整合 redis实现分布式锁

**使用分布式锁锁商品库存，防止超卖**

**OmsOrderServiceImpl订单service实现类中生成订单方法**

```java
@Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateOrder(UmsMember umsMember) throws Exception {
        //查询该用户加入购物车的所有商品信息
        List<OmsCartDetail> cartDetails = omsCartDetailService.queryCartDetailByMemberId(umsMember.getId());

        //根据购物车中商品下订单，计算总金额
        List<OmsOrderDetail> omsOrderDetails = new ArrayList<>();

        BigDecimal sum = new BigDecimal(0);
        for (OmsCartDetail ocd : cartDetails) {

            Long productId = ocd.getProductId();
            //使用redis分布式锁 锁库存
            RedisLock.lock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));
            //减库存
            PmsSkuStock pmsSkuStock = new PmsSkuStock();
            pmsSkuStock.setStock(-ocd.getQuantity());
            pmsSkuStock.setProductId(ocd.getProductId());
            int stockResult = pmsSkuStockService.updateProductStock(pmsSkuStock);
            if (stockResult == 0) {
                //未能锁定库存，直接释放分布式锁，并抛出异常
                RedisLock.unLock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));
                throw new Exception("商品售罄！" + ocd.getProductId() + " : " + ocd.getProductName());
            }
            //释放锁
            RedisLock.unLock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));

            //创建订单详情
            OmsOrderDetail omsOrderDetail = new OmsOrderDetail();
            omsOrderDetail.setMemberId(umsMember.getId());
            omsOrderDetail.setProductId(productId);
            omsOrderDetail.setProductName(ocd.getProductName());
            omsOrderDetail.setQuantity(ocd.getQuantity());
            omsOrderDetail.setRealPrice(ocd.getPrice());
            omsOrderDetails.add(omsOrderDetail);
            //计算总金额
            sum = sum.add(ocd.getPrice());
        }

        //创建订单信息
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setMemberId(umsMember.getId());
        omsOrder.setMemberUsername(umsMember.getUsername());
        omsOrder.setSourceType(2);
        omsOrder.setStatus(0);
        omsOrder.setTotalAmount(sum);
        omsOrder.setDeleteStatus(0);
        omsOrder.setOrderType(1);
        omsOrder.setPayType(1);
        omsOrder.setReceiverName(umsMember.getUsername());
        omsOrder.setReceiverPhone(umsMember.getTelephone());
        //生成订单，并获取订单号
        int orderResult = omsOrderMapper.insertOrder(omsOrder);

        Long orderId = omsOrder.getId();
        for (OmsOrderDetail ood : omsOrderDetails) {
            //设置订单详情订单号
            ood.setOrderId(orderId);
        }
        //添加订单详情
        int orderDetailResult = omsOrderDetailMapper.insertOrderDetailList(omsOrderDetails);

        //设置redis 订单超时时间，超时30分钟未支付自动取消订单，并释放商品，返回库存
        //这里为了测试，设置为1分钟
        redisTemplate.opsForValue().setIfAbsent("Order_" + omsOrder.getId(), omsOrder.getStatus(), Duration.ofMinutes(1));
        log.info("========== Set order key in redis: " + "Order_" + omsOrder.getId());
        return orderId;
    }
```

RedisLock类实现redis加锁解锁

```java
package distributed.cache.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisLock {

    /**
     * redis分布式锁-加锁
     *
     * @param redisTemplate redis 加锁解锁要保证同一个
     * @param key           分布式锁key
     * @param value         分布式锁value 一般为随机数
     * @param timeout       分布式锁过期时间 秒
     * @param number        重试次数
     * @param interval      重试间隔 毫秒
     * @return
     */
    public static boolean setLock(RedisTemplate redisTemplate, String key, String value, int timeout, int number, int interval) {
        //加锁
        for (int i = 0; i < number; i++) {
            //尝试获取锁,成功则返回不成功则重试
            if (redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(timeout))) {
                log.info("========== Set the redis lock of key:" + key + " successfully!");
                return true;
            }
            //暂停
            try {
                TimeUnit.MILLISECONDS.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //最终获取不到锁返回失败
        log.error("========== Failed to set the redis lock of key: " + key + " !");
        return false;
    }

    /**
     * redis分布式锁-加锁
     *
     * @param redisTemplate redis 加锁解锁要保证同一个
     * @param key           分布式锁key
     * @param value         分布式锁value 一般为随机数
     * @return
     */
    public static boolean lock(RedisTemplate redisTemplate, String key, String value) {
        return setLock(redisTemplate, key, value, 30, 3, 1000);
    }

    /**
     * 解锁LUA脚本,防止线程将其他线程的锁释放
     */
    private static String UN_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * redis分布式锁-解锁
     *
     * @param redisTemplate redis 加锁解锁要保证同一个
     * @param key           分布式锁key
     * @param value         分布式锁value 一般为随机数
     * @return
     */
    public static void unLock(RedisTemplate redisTemplate, String key, String value) {
        //解锁
        redisTemplate.execute(new DefaultRedisScript<>(UN_LOCK_LUA_SCRIPT, Long.class),
                Collections.singletonList(key), value);
    }
}

```



## 1.2.后台数据库相关SQL

### 1.2.1.建库

**分库：tomato_shop_0、tomato_shop_1**

```sql
drop database if exists tomato_shop_0;
drop database if exists tomato_shop_1;
create database IF NOT EXISTS tomato_shop_0 default charset utf8mb4;
create database IF NOT EXISTS tomato_shop_1 default charset utf8mb4;
```

### 1.2.2.创建表

**分表：**

**创建订单表**

**每个库分别创建表 oms_order_0  到 oms_order_7，共8张表**

```sql
DROP TABLE IF EXISTS oms_order_0;

CREATE TABLE `oms_order_0` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `member_id` bigint NOT NULL COMMENT '用户id',
  `source_type` tinyint NOT NULL COMMENT '订单来源：1->APP;2->网页', 
  `member_username` varchar(64) NOT NULL COMMENT '用户帐号',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1->支付宝；2->微信; 3->其他',  
  `status` tinyint DEFAULT NULL COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
  `order_type` tinyint DEFAULT NULL COMMENT '订单类型：0->正常订单；1->秒杀订单',  
  `receiver_name` varchar(100) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `confirm_status` tinyint DEFAULT NULL COMMENT '确认收货状态：0->未确认；1->已确认',
  `delete_status` tinyint NOT NULL DEFAULT '0' COMMENT '删除状态：0->未删除；1->已删除',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT NULL COMMENT '提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='订单表';
```

**创建商品表**

```sql
DROP TABLE IF EXISTS pms_product;

CREATE TABLE `pms_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌id',
  `product_category_id` bigint DEFAULT NULL COMMENT '商品分类id',
  `product_attribute_category_id` bigint DEFAULT NULL COMMENT '商品属性分类id',
  `name` varchar(64) NOT NULL COMMENT '商品名称',
  `pic` varchar(255) DEFAULT NULL,
  `product_sn` varchar(64) NOT NULL COMMENT '货号',
  `delete_status` tinyint DEFAULT NULL COMMENT '删除状态：0->未删除；1->已删除',
  `publish_status` tinyint DEFAULT NULL COMMENT '上架状态：0->下架；1->上架',
  `verify_status` tinyint DEFAULT NULL COMMENT '审核状态：0->未审核；1->审核通过',
  `sort` int DEFAULT NULL COMMENT '排序',
  `sale` int DEFAULT NULL COMMENT '销量',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '市场价',
  `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题',
  `description` varchar(255) COMMENT '商品描述',
  `stock` int DEFAULT NULL COMMENT '库存',
  `low_stock` int DEFAULT NULL COMMENT '库存预警值',
  `unit` varchar(16) DEFAULT NULL COMMENT '单位',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '商品重量，默认为克',
  `keywords` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='商品信息';
```

**创建商品库存表**

```sql
DROP TABLE IF EXISTS `pms_sku_stock`;
CREATE TABLE `pms_sku_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `sku_code` varchar(64) NOT NULL COMMENT 'sku编码',
  `stock` int(11) DEFAULT '0' COMMENT '库存',
  `low_stock` int(11) DEFAULT NULL COMMENT '预警库存',
  `pic` varchar(255) DEFAULT NULL COMMENT '展示图片',
  `sale` int(11) DEFAULT NULL COMMENT '销量',
  `lock_stock` int(11) DEFAULT '0' COMMENT '锁定库存',
  `sp_data` varchar(500) DEFAULT NULL COMMENT '商品销售属性，json格式',
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='sku的库存';
```

**创建订单详情表**

**每个库分别创建表 oms_order_detail_0  到 oms_order_detail_7，共8张表**

```sql
DROP TABLE IF EXISTS `oms_order_detail_0`;
CREATE TABLE `oms_order_detail_0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `member_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `product_pic` varchar(500) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_brand` varchar(200) DEFAULT NULL,
  `product_sn` varchar(64) DEFAULT NULL,
  `product_price` decimal(10,2) DEFAULT NULL COMMENT '销售价格',
  `real_price` decimal(10,2) DEFAULT NULL COMMENT '该商品经过优惠后的价格',
  `quantity` int(11) DEFAULT NULL COMMENT '购买数量',
  `product_sku_id` bigint(20) DEFAULT NULL COMMENT '商品sku编号',
  `product_sku_code` varchar(64) DEFAULT NULL COMMENT '商品sku条码',
  `product_category_id` bigint(20) DEFAULT NULL COMMENT '商品分类id',
  `product_attr` varchar(500) DEFAULT NULL COMMENT '商品销售属性:[{"key":"颜色","value":"颜色"},{"key":"容量","value":"4G"}]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单详情表';
```

**创建购物车表**

```sql
DROP TABLE IF EXISTS `oms_cart_detail`;
CREATE TABLE `oms_cart_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL,
  `product_sku_id` bigint(20) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL COMMENT '购买数量',
  `price` decimal(10,2) DEFAULT NULL COMMENT '添加到购物车的价格',
  `product_pic` varchar(1000) DEFAULT NULL COMMENT '商品主图',
  `product_name` varchar(500) DEFAULT NULL COMMENT '商品名称',
  `product_sub_title` varchar(500) DEFAULT NULL COMMENT '商品副标题（卖点）',
  `product_sku_code` varchar(200) DEFAULT NULL COMMENT '商品sku条码',
  `member_nickname` varchar(500) DEFAULT NULL COMMENT '会员昵称',
  `delete_status` int(1) DEFAULT '0' COMMENT '是否删除',
  `product_category_id` bigint(20) DEFAULT NULL COMMENT '商品分类',
  `product_brand` varchar(200) DEFAULT NULL,
  `product_sn` varchar(200) DEFAULT NULL,
  `product_attr` varchar(500) DEFAULT NULL COMMENT '商品销售属性:[{"key":"颜色","value":"颜色"},{"key":"容量","value":"4G"}]',
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='购物车表';
```



# 2. 基于redis pub/sub消息发布订阅模式实现订单异步处理

## 2.1.配置RedisListenerConfig配置类

**配置RedisListenerConfig配置类实现redis缓存过期通知以及消息订阅监听配置**

```java
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
```

## 2.2.配置RedisKeyExpirationListener缓存过期监听器

**onMessage方法在redis key过期时会回调**

**模拟订单超时未支付场景**

**获取超时未支付订单号，基于redis publish 发送取消订单消息**

```java
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

```

## 2.3.配置RedisConsumer消息消费方

**receiveMessage方法用于接收订阅的消息，获取消息中的订单ID，触发取消订单动作**

```java
package distributed.cache.redis.consumer;

import distributed.cache.redis.entity.OmsOrder;
import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.service.OmsOrderDetailService;
import distributed.cache.redis.service.OmsOrderService;
import distributed.cache.redis.service.PmsSkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RedisConsumer {
    @Autowired
    OmsOrderService omsOrderService;
    @Autowired
    OmsOrderDetailService omsOrderDetailService;
    @Autowired
    PmsSkuStockService pmsSkuStockService;

    /**
     * redis订阅推送的消息 会回调该方法  message：推送的信息
     */
    public void receiveMessage(String message) {
        //根据订单编号取消订单
        System.out.println(message);
        //因为接收到的消息前后多出了双引号，这里需要截取字符串
        String subStr = message.substring(1, message.length() - 1);

        Long orderId = Long.valueOf(subStr);
        //设置订单状态为无效订单 status - 5
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setStatus(5);
        omsOrder.setId(orderId);
        int result = omsOrderService.cancelOrder(omsOrder);
        //查询订单详情，根据详情释放锁定库存
        if (result != 0) {
            List<OmsOrderDetail> omsOrderDetails = omsOrderDetailService.queryOrderDetailsByOrderId(orderId);
            int returnStock = pmsSkuStockService.returnStock(omsOrderDetails);
            if (returnStock != 0) {
                log.info("========== Order:{} has been canceled and the stock has been returned.", orderId);
            }
        }
    }
}

```
