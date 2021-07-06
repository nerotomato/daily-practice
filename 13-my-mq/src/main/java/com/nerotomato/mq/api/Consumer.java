package com.nerotomato.mq.api;

import java.util.List;

/**
 * 消费者接口
 */
public interface Consumer {

    /**
     * 获取数据
     * 返回至多最大值 num 的数据量
     *
     * @param num 最大数据量
     * @return data list
     */
    List poll(int num);
}
