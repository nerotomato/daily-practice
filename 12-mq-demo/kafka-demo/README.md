

[TOC]

# 1.下载kafka应用程序

```shell
wget https://mirrors.tuna.tsinghua.edu.cn/apache/kafka/2.8.0/kafka_2.13-2.8.0.tgz
```

**解压**

```shell
tar -zxvf kafka_2.13-2.8.0.tgz
```

# 2.启动服务和停止服务

**kafka需要使用Zookeeper，首先需要启动Zookeeper服务，如果没有的话，可以使用kafka自带的脚本启动一个简单的单一节点Zookeeper实例**

**启动zookeeper**

```shell
cd 到bin目录
./zookeeper-server-start.sh ../config/zookeeper.properties &
(使用 bin/zookeeper-server-start.sh -daemon config/zookeeper.properties 以守护进程启动)
```

**注意修改配置文件server.properties**

```shell
命令行下进入 kafka 目录
修改配置文件 vim config/server.properties
放开注释并改成需要的ip地址

listeners=PLAINTEXT://localhost:9092
```

**启动kafka服务**

```shell
bin/kafka-server-start.sh config/server.properties &
```

**停止kafka服务**

```shell
bin/kafka-server-stop.sh config/server.properties
```

# 3.Kafka简单使用

**创建主题**

**创建一个名为testTopic的topic，只使用单个分区和一个复本**

```shell
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic testTopic
```

**删除主题**

**下面的操作表示该主题仅仅标记为“待删除”，至于topic是否被真正删除取决于broker端（server.properties）的参数设置delete.topic.enable,若设置为false，那么即使运行了上面的命令，主题也不会被删除。需要说明的是主题的删除时异步的，就算将delete.topic.enable**

**设置为true，当执行了上面的命令后，也需要根据主题的分片依次进行删除。**

```shell
bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic testTopic
```

**查看有哪些主题**

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --list
```

**查看主题**

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic testTopic
```

**发送消息**

```shell
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic testTopic
```

**启动一个消费者,消费者会接收到消息**

```shell
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic testTopic --from-beginning
```

**简单性能测试**

**测试消息写入性能**

```properties
#命令参数解析
-topic topic名称，本例为testTopic
--num-records 总共需要发送的消息数，本例为100000
--record-size 每个记录的字节数，本例为1000
--throughput 每秒钟发送的记录数，本例为5000
--producer-props bootstrap.servers=localhost:9092 （发送端的配置信息，本次测试取集群服务器中的一台作为发送端,可在kafka的config目录，查看server.properties中配置的zookeeper.connect的值，默认端口：9092）
```

```shell
bin/kafka-producer-perf-test.sh --topic testTopic --num-records 100000 --record-size 1000 --throughput 2000 --producer-props bootstrap.servers=localhost:9092
```

**测试消息消费性能**

```properties
#命令参数解析
kafka-consumer-perf-test.sh 脚本命令的参数为：
--zookeeper 指定zookeeper的链接信息，本例为localhost:2181 ；
--topic 指定topic的名称，本例为testTopic
--fetch-size 指定每次fetch的数据的大小，本例为1048576，也就是1M
--messages 总共要消费的消息个数，本例为1000000，100w
```

```shell
bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9092 --topic testTopic --fetch-size 1048576 --messages 100000 --threads 1
```

# 4.搭建kafka集群

**创建3个节点的配置文件**

**server_9001.properties**

**server_9002.properties**

**server_9003.properties**

**三个配置文件主要需要区分的配置参数如下，其他配置一样即可**

```properties
broker.id=1
listeners=PLAINTEXT://localhost:9001
log.dirs=/tmp/kafka-logs-1
```

```properties
broker.id=2
listeners=PLAINTEXT://localhost:9002
log.dirs=/tmp/kafka-logs-2
```

```properties
broker.id=3
listeners=PLAINTEXT://localhost:9003
log.dirs=/tmp/kafka-logs-3
```



**启动zookeeper**

```shell
cd到kafka bin目录
./zookeeper-server-start.sh ../config/zookeeper.properties &
(使用 bin/zookeeper-server-start.sh -daemon config/zookeeper.properties 以守护进程启动)
```

**搭建集群，启动三个kafka节点服务**

```shell
cd到kafka bin目录
./kafka-server-start.sh ../config/server_9001.properties &
./kafka-server-start.sh ../config/server_9002.properties &
./kafka-server-start.sh ../config/server_9003.properties &
```

**查看有哪些主题**

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --list
```

**创建带有副本的 topic**

**创建有三个分区，两个副本的topic**

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic testTopic --partitions 3 --replication-factor 2
```

**查看主题**

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic testTopic
```

**生产消息**

```shell
bin/kafka-console-producer.sh --bootstrap-server localhost:9003 --topic testTopic
```

**消费消息**

```shell
bin/kafka-console-consumer.sh --bootstrap-server localhost:9001 --topic testTopic --from-beginning
```



**集群性能测试**

**队列写入性能测试**

```shell
bin/kafka-producer-perf-test.sh --topic testTopic --num-records 100000 --record-size 1000 --throughput 200000 --producer-props bootstrap.servers=localhost:9002
```

**队列消费性能测试**

```shell
bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9002 --topic testTopic --fetch-size 1048576 --messages 100000 --threads 1
```



**kafka集群备份策略**

消息以partition为单位分配到多个server，并以partition为单位进行备份。备份策略为：1个leader和N个followers，leader接受读写请求，followers被动复制leader。leader和followers会在集群中打散，保证partition高可用。

kafka 将每个 partition 数据复制到多个 server 上,任何一个partition有一个leader和多个follower(可以没有);备份的个数可以通过 broker 配置文件来设定。leader 处理所有的 read-write 请求,follower 需要和 leader 保持同步。Follower 和 consumer 一样,消费消息并保存在本地日志中;leader 负责跟踪所有的 follower 状态,如果follower”落后”太多或者失效,leader将会把它从replicas同步列表中删除。当所有的 follower 都将一条消息保存成功,此消息才被认为是”committed”,那么此时 consumer 才能消费它。即使只有一个 replicas 实例存活,仍然可以保证消息的正常发送和接收,只要zookeeper 集群存活即可。(不同于其他分布式存储,比如 hbase 需要”多数派”存活才行)当leader失效时,需在followers中选取出新的leader,可能此时 follower 落后于 leader,因此需要选择一个”up-to-date”的follower。选择follower时需要兼顾一个问题,就是新leader server上所已经承载的 partition leader 的个数,如果一个 server 上有过多的 partition leader,意味着此 server 将承受着更多的IO 压力。在选举新 leader,需要考虑到”[负载均衡](https://cloud.tencent.com/product/clb?from=10680)”

# 5.kafka集群监控

**github下载yahoo/kafka-manager，最新版的名字已改成yahoo/CMAK**

**下载源码包CMAK-3.0.0.5.tar.gz，解压并编译。注意，最新版的代码需要依赖JDK11版本**

```shell
#检查自己环境的jdk版本是否是java11版本
java -version
#切换到源码包解压出来的路径
cd /home/nerotomato/Develop/java_develop/mq/CMAK-3.0.0.5
#执行命令编译
./sbt clean dist
#等待编译结束后，找到编译完成的zip包
ll /home/nerotomato/Develop/java_develop/mq/CMAK-3.0.0.5/target/universal/cmak-3.0.0.5.zip
#将编译好的zip包移动到自己喜欢的位置
cd /home/nerotomato/Develop/java_develop/mq/CMAK-3.0.0.5/target/universal
mv cmak-3.0.0.5.zip ~/Develop/java_develop/mq/
#删除同名的源码包文件夹
rm -rf ~/Develop/java_develop/mq/CMAK-3.0.0.5
#解压编译好的zip包
unzip cmak-3.0.0.5.zip

#切换到解压路径
cd /home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5
```

**启动zk集群，kafka集群，再启动kafka-manager服务。**

**bin/kafka-manager 默认的端口是9000，可通过 -Dhttp.port，指定端口; -Dconfig.file=conf/application.conf指定配置文件:**

**修改application.conf配置文件,将cmak.zkhosts改为自己的zookeeper地址**

```properties
cmak.zkhosts="localhost:2181"
```

**或者使用环境变量，这样就不用修改配置文件了**

**`ZK_HOSTS`如果您不想硬编码任何值，请使用环境变量。**

```shell
ZK_HOSTS="localhost:2181"
```

```shell
#切换到解压路径
cd /home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5
nohup bin/cmak -Dconfig.file=conf/application.conf -Dhttp.port=9000 &
```

**编写启动脚本，方便启动**

```shell
vim cmak-start.sh
#需要指定JAVA_HOME环境变量为java11，否则监控界面访问会报错，日志显示JDK版本错误
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/
nohup /home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5/bin/cmak -Dconfig.file=/home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5/conf/application.conf -Dhttp.port=9000 > /home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5/logs/application.log 2>&1 &
#添加可执行权限
chmod +x cmak-start.sh
```

**编写停止脚本**

```shell
vim cmak-stop.sh

cd /home/nerotomato/Develop/java_develop/mq/cmak-3.0.0.5
cat RUNNING_PID | xargs kill -9
rm RUNNING_PID
#添加可执行权限
chmod +x cmak-stop.sh
```

**浏览器访问http://localhost:9000/**

点击【Cluster】>【Add Cluster】打开如下添加集群配置界面：输入集群的名字（如`Kafka-Cluster-1`）和 Zookeeper 服务器地址（如`localhost:2181`），选择最接近的Kafka版本

查看新建的集群配置kafka-cluster-1

![image-20210701155132836](/home/nerotomato/.config/Typora/typora-user-images/image-20210701155132836.png)

查看Topic信息

![image-20210701155712016](/home/nerotomato/.config/Typora/typora-user-images/image-20210701155712016.png)





![image-20210701155812948](/home/nerotomato/.config/Typora/typora-user-images/image-20210701155812948.png)



# 6.Springboot整合kafka

**引入kafka依赖**

```xml
 <!--引入kafka的依赖-->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>       
```

**springboot配置文件application.yml**

```yaml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: 127.0.0.1:9001,127.0.0.1:9002,127.0.0.1:9003 #指定kafka server的地址，集群配多个，中间，逗号隔开
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: default_consumer_group #群组ID
      enable-auto-commit: true
      auto-commit-interval: 1000
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

**简单生产者**

```java
package com.nerotomato.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 简单生产者
 */
@Component
public class ProducerDemo {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String message) {
        kafkaTemplate.send("testTopic", message);
    }
}
```

**简单消费者**

```java
package com.nerotomato.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * 简单消费者
 * */
@Slf4j
@Component
public class ConsumerDemo {

    // 消费监听
    @KafkaListener(topics = {"testTopic"})
    public void onMessage(ConsumerRecord<?, ?> record) {
        // 消费的哪个topic、partition的消息,打印出消息内容
        log.info("======> Message received : " + "Topic: " + record.topic() + ",Partition: " + record.partition() + ",Value: " + record.value());
    }
}
```

**前端控制器controller**

```java
package com.nerotomato.kafka.contorller;

import com.nerotomato.kafka.producer.ProducerDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaDemoController {

    @Autowired
    ProducerDemo producer;

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public void sendMessage(@RequestParam(value = "message") String message) {
        producer.send(message);
    }
}
```

**springboot启动类**

```java
package com.nerotomato.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaDemoApplication.class, args);
    }
}
```

