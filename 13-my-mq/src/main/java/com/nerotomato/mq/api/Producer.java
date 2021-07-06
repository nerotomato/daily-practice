package com.nerotomato.mq.api;

/**
 * 生产者接口
 */
public interface Producer {
    /**
     * @param topic   主题
     * @param content 内容
     */
    Object send(String topic, String content);
}
