[TOC]

# 1.版本一之基于内存的queue

**采用LinkedBlockingQueue 内存队列的形式，自定义实现消息队列的功能**

```java
package com.nerotomato.mq.core;

import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyMq {
    private String topic;
    private int capacity;
    private LinkedBlockingQueue<MyMqMessage> queue;

    public MyMq(String topic, int capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public boolean send(MyMqMessage myMqMessage) {
        return queue.offer(myMqMessage);
    }

    public MyMqMessage poll(){
        return queue.poll();
    }

    @SneakyThrows
    public MyMqMessage poll(long timeout) {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }
}

```

```java
package com.nerotomato.mq.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MyMqBroker {
    public static final int CAPACITY = 10000;

    private final Map<String, MyMq> myMqMap = new ConcurrentHashMap<>(64);

    public void createTopic(String name) {
        myMqMap.putIfAbsent(name, new MyMq(name, CAPACITY));
    }

    public MyMq findMyMq(String topic) {
        return this.myMqMap.get(topic);
    }

    public MyMqProducer createProducer() {
        return new MyMqProducer(this);
    }

    public MyMqConsumer createConsumer() {
        return new MyMqConsumer(this);
    }
}

```

```java
package com.nerotomato.mq.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class MyMqMessage<T> {
    private HashMap<String, Object> headers;
    private T body;
}

```

```java
package com.nerotomato.mq.core;

public class MyMqProducer {

    private MyMqBroker myMqBroker;

    public MyMqProducer(MyMqBroker myMqBroker) {
        this.myMqBroker = myMqBroker;
    }

    public boolean send(String topic, MyMqMessage message) {
        MyMq myMq = myMqBroker.findMyMq(topic);
        if (null == myMq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
        return myMq.send(message);
    }
}

```

```java
package com.nerotomato.mq.core;

public class MyMqConsumer {

    private MyMqBroker myMqBroker;
    private MyMq myMq;

    public MyMqConsumer(MyMqBroker myMqBroker) {
        this.myMqBroker = myMqBroker;
    }

    public void subscribe(String topic) {
        myMq = myMqBroker.findMyMq(topic);
        if (null == myMq) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
    }

    public MyMqMessage poll(long timeout) {
        return myMq.poll(timeout);
    }
}

```



# **2.版本二之自定义内存Queue**

- **1）自定义内存Message数组模拟Queue。**
-  **2）使用指针记录当前消息写入位置。**
-  **3）对于每个命名消费者，用指针记录消费位置。**



**去掉内存Queue，设计自定义Queue，实现消息确认和消费offset**

**存储队列采用CopyOnWriteArrayList实现**

```java
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
```

**自定义broker**

```java
package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomBroker {

    private Map<String, CustomQueue> queueMap = new ConcurrentHashMap<>();

    /**
     * broker直接发送消息
     */
    public boolean send(String topic, CustomMessage message) {
        CustomQueue queue = queueMap.getOrDefault(topic, new CustomQueue(topic));
        queue.add(message);
        return true;
    }

    /**
     * broker直接消费消息
     */
    public List<CustomMessage> poll(String topic, String group, int num) {
        CustomQueue queue = queueMap.get(topic);
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == queue) {
            return messages;
        }
        log.info("queue message amount : " + queue.size());
        while (!queue.isEmpty() || num > 0) {
            CustomMessage message = queue.get(group);
            if (message == null) {
                break;
            }
            messages.add(message);
            num -= 1;
        }
        return messages;
    }

    /**
     * 根据topic查找队列
     */
    public CustomQueue findCustomQueue(String topic) {
        return this.queueMap.get(topic);
    }

    /**
     * 创建生产者和topic消息队列
     */
    public CustomProducer createProducer(String topic) {
        queueMap.putIfAbsent(topic, new CustomQueue(topic));
        return new CustomProducer(this);
    }

    /**
     * 创建消费者
     */
    public CustomConsumer createConsumer() {
        return new CustomConsumer(this);
    }
}

```

**自定义生产者**

```java
package com.nerotomato.mq.core.custom;

public class CustomProducer {

    private CustomBroker customBroker;

    public CustomProducer(CustomBroker customBroker) {
        this.customBroker = customBroker;
    }

    public boolean send(String topic, CustomMessage message) {
        CustomQueue customQueue = customBroker.findCustomQueue(topic);
        if (null == customQueue) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
        return customQueue.add(message);
    }

}

```

**自定义消费者**

```java
package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CustomConsumer {
    /**
     * 消费者所属组
     */
    private String group;
    private CustomBroker customBroker;
    private CustomQueue customQueue;

    public CustomConsumer(CustomBroker customBroker) {
        this.group = Thread.currentThread().getId() + " - " + Thread.currentThread().getName();
        this.customBroker = customBroker;
    }

    /**
     * 消费者订阅topic
     */
    public void subscribe(String topic) {
        customQueue = customBroker.findCustomQueue(topic);
        if (null == customQueue) {
            throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        }
    }

    /**
     * 消费者 消费消息
     * 可指定消费-num数量的消息
     */
    public List<CustomMessage> poll(String topic, int num) {
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == customQueue) {
            return messages;
        }
        log.info("queue message amount : " + customQueue.size());
        while (!customQueue.isEmpty() || num > 0) {
            CustomMessage message = customQueue.get(group);
            if (message == null) {
                break;
            }
            messages.add(message);
            num -= 1;
        }
        return messages;
    }
}

```

# **3.版本三之基于SpringMVC实现MQServer**



**修改broker，添加broker发送消费消息的方法**

```java
package com.nerotomato.mq.core.custom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomBroker {

    /**
     * 消息队列集合
     * key - topic
     * value - CustomQueue
     */
    private Map<String, CustomQueue> queueMap = new ConcurrentHashMap<>();

    /**
     * 拆分producer和consumer
     * 由broker发送消息
     */
    public boolean send(String topic, CustomMessage message) {
        CustomQueue queue = queueMap.get(topic);
        if (null == queue) {
            queue = new CustomQueue(topic);
            queueMap.put(topic, queue);
        }
        queue.add(message);
        return true;
    }

    /**
     * 拆分producer和consumer
     * 从broker消费消息
     * num > 0 表示指定消费 num 数量的消息
     * num=-1 表示消费队列剩余全部消息
     */
    public List<CustomMessage> poll(String topic, String group, int num) {
        CustomQueue queue = queueMap.get(topic);
        LinkedList<CustomMessage> messages = new LinkedList<>();
        if (null == queue) {
            return messages;
        }
        log.info("queue message amount : " + queue.size());
        if (num == -1) {
            while (!queue.isEmpty()) {
                CustomMessage message = queue.get(group);
                if (message == null) {
                    break;
                }
                messages.add(message);
            }
            return messages;
        }
        while (!queue.isEmpty() && num > 0) {
            CustomMessage message = queue.get(group);
            if (message == null) {
                break;
            }
            messages.add(message);
            num -= 1;
        }
        return messages;
    }

    /**
     * 根据topic查找队列
     */
    public CustomQueue findCustomQueue(String topic) {
        return this.queueMap.get(topic);
    }

    /**
     * 创建生产者和topic消息队列
     */
    public CustomProducer createProducer(String topic) {
        queueMap.putIfAbsent(topic, new CustomQueue(topic));
        return new CustomProducer(this);
    }

    /**
     * 创建消费者
     */
    public CustomConsumer createConsumer() {
        return new CustomConsumer(this);
    }

    /**
     * 创建消息队列
     */
    public Object createTopic(String topic) {
        CustomQueue queue = findCustomQueue(topic);
        if (null == queue) {
            queue = new CustomQueue(topic);
            queueMap.put(topic, queue);
            return "The topic [ " + topic + " ] has benn created successfully!";
        }
        return "The topic [ " + topic + " ] is exist.";
    }
}

```

**基于SpringMVC 添加Contorller 生产消费消息的api** 

```java
package com.nerotomato.mq.controller;

import com.nerotomato.mq.core.custom.CustomBroker;
import com.nerotomato.mq.core.custom.CustomMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/custom")
public class CustomMqController {

    @Autowired
    CustomBroker customBroker;

    @RequestMapping(value = "/createTopic", method = RequestMethod.POST)
    public Object create(@RequestParam(value = "topic") String topic) {
        return customBroker.createTopic(topic);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public Object send(@RequestBody CustomMessage message) {

        log.info("======> Start sending message to {}", message.getTopic());
        long startTime = System.currentTimeMillis();
        boolean send = customBroker.send(message.getTopic(), message);
        long endTime = System.currentTimeMillis();
        log.info("The time cost of producer is {}", endTime - startTime);
        return send;
    }

    @RequestMapping(value = "/poll", method = RequestMethod.GET)
    public Object poll(@RequestParam(value = "group") String group,
                       @RequestParam(value = "topic") String topic,
                       @RequestParam(value = "num") int num) {
        List<CustomMessage> customMessages = null;
        while (true) {
            customMessages = customBroker.poll(topic, group, num);
            break;
        }
        return customMessages;
    }

}
```

**创建生产者和消费者，并使用Spring自带的RestTemplate发送http请求**

```java
package com.nerotomato.mq.api;

import com.nerotomato.mq.core.custom.CustomMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class HttpProducer implements Producer {

    /**
     * Spring自带http请求工具 RestTemplate 阻塞式
     */
    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String, Object> properties;

    public HttpProducer(Map<String, Object> properties) {
        this.properties = properties;
    }


    @Override
    public Object send(String topic, String content) {
        String url = properties.get("url").toString();
        String brokerUrl = url + "/custom/send?topic=" + topic + "&content=" + content;
        HttpEntity<CustomMessage<String>> httpEntity = new HttpEntity<>(new CustomMessage<>(null, topic, content));
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(brokerUrl, httpEntity, Object.class);
        return responseEntity.getBody();
    }
}

```

```java
package com.nerotomato.mq.api;

import com.nerotomato.mq.core.custom.CustomMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpConsumer implements Consumer {

    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String, Object> properties;

    public HttpConsumer(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public List<LinkedHashMap> poll(int num) {
        String topic = properties.get("topic").toString();
        String group = properties.get("group").toString();
        String url = properties.get("url").toString();
        String brokerUrl = url + "/custom/poll?topic=" + topic + "&group=" + group + "&num=" + num;
        long startTime = System.currentTimeMillis();
        ResponseEntity<List> responseEntity = restTemplate.getForEntity(brokerUrl, List.class);
        long endTime = System.currentTimeMillis();
        log.info("The time cost of Consumer is {} milliseconds", endTime - startTime);
        return responseEntity.getBody();
    }
}
```

```java
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

```

```java
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
```

