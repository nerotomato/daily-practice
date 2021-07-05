package com.nerotomato.mq.core.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class CustomQueue {

    /**
     * 队列名称
     */
    private String topic;

    /**
     * 存储队列
     */
    private CopyOnWriteArrayList<CustomMessage> queue;

    /**
     * 各个分组的读取位置记录
     * <p>
     * group --> index
     */
    private Map<String, AtomicInteger> groupOffset = new ConcurrentHashMap<>();


    /**
     * 写位置记录
     */
    private int writeIndex = 0;

    public CustomQueue(String topic) {
        this.topic = topic;
        queue = new CopyOnWriteArrayList<>();
    }


    /**
     * 发送消息
     */
    public boolean add(CustomMessage message) {
        queue.add(message);
        writeIndex += 1;
        return true;
    }


    /**
     * 获取消息
     * 判断读取到的offset位置
     *
     * @return
     */
    public CustomMessage get(String group) {
        // getOrDefault()方法：获取指定 key 对应对 value，如果找不到 key ，则返回设置的默认值-1
        //index = 获取到的AtomicInteger + 1
        int index = 0;
        AtomicInteger groupInteger = groupOffset.get(group);

        if (null == groupInteger) {
            index = 0; // 不存在群组时，添加该群组信息，并设置index=0，从第0位开始读取
            groupOffset.put(group, new AtomicInteger(index));
        } else {
            //群组存在时，获取上次读取的位置信息，并且 index + 1
            index = groupOffset.get(group).incrementAndGet();
        }

        //int index = groupOffset.getOrDefault(group, new AtomicInteger(-1)).incrementAndGet();
        //当前群组读取的index >= 队列的大小了，说明读取完毕
        if (writeIndex == 0 || index >= queue.size()) {
            return null;
        }
        return queue.get(index);
    }

    public boolean isEmpty() {
        return writeIndex == 0;
    }

    public int size() {
        return writeIndex;
    }

}
